package com.training.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.training.entity.AnalysisEntity;
import com.training.entity.FilesByUserEntity;
import com.training.entity.MetaResultEntity;
import com.training.entity.ProcessEntity;
import com.training.entity.ResponseList;
import com.training.exception.AppErrorCodeEnum;
import com.training.exception.AppException;
import com.training.exception.RemoteException;
import com.training.model.FileEntityWithProcessListWrapper;
import com.training.model.FileSizeCapacityJSON;
import com.training.model.UserInfo;
import com.training.model.UserSession;
import com.training.service.AnalysisService;
import com.training.service.FileService;
import com.training.service.MetaService;
import com.training.service.ProcessService;
import com.training.service.SampleServiceData;
import com.training.utility.Constants;
import com.training.utility.GoogleUploadUtility;
import com.training.utility.LocalUploadUtility;
import com.training.utility.MicrobiomeRestDelegate;
import com.training.utility.PropertiesSingletonBean;
import com.training.utility.UploadUtility;

@Path("/fileservice")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FileMetaEndPoint {



	private static final Logger LOGGER = LogManager.getLogger(FileMetaEndPoint.class.getName());

	@Inject
	FileService fileService;
	
	@Inject
	ProcessService projectService;
	
	@Inject
	AnalysisService analysisService;
	
	@Inject
	MetaService metaService;
	
	@Inject
	SampleServiceData sampleServiceData;
	
	@Context
	SecurityContext securityContext;
	
	@Inject
	MicrobiomeRestDelegate restClient;
	
	@POST
	@Path("/v1/files/{userid}")
	@Consumes("multipart/form-data")
	@Produces("application/json")
	public Response uploadFile(@HeaderParam("Content-Length") int fileLength, MultipartFormDataInput multipartFormDataInput,
			@PathParam(value = "userid") String userId,
			@QueryParam("createProjectFlag") @DefaultValue("true") Boolean createProject,
			@QueryParam("detailsFlag") @DefaultValue("false") Boolean detailsFlag,
			@QueryParam("sessionId") @DefaultValue("sessionId") String sessionIdParam, @Context HttpHeaders headers) throws AppException {

		UploadUtility uploadService = null;
		//UserInfo userInfo1 = (UserInfo) securityContext.getUserPrincipal();
		// boolean uploadStatus = false;
		List<String> result = new ArrayList<String>();
		String OVERALL_RESPONSE = Constants.FAIL;
		String processIDs = "";
		boolean isEligibleForUpload = false;
		List<FilesByUserEntity> projectFileList = null;
		FilesByUserEntity filesByUserEntity = null;
		LOGGER.info("file upload service /v1/files/ start");
		try {
			if (validateJSONAndFileExtn(multipartFormDataInput) == false) {
				throw new AppException(javax.ws.rs.core.Response.Status.BAD_REQUEST.getStatusCode(),
						AppErrorCodeEnum.INVALID_INPUT_FILE_EXTN.getErrorCode(), "Invalid File Extension");
			}
			/*** Add validation based on allowed file extensions */
			String modeEnabled = PropertiesSingletonBean.getProperty(Constants.MODE_DEBUG);
			projectFileList = fileService.fetchByUserId(UUID.fromString(userId));// Fetch
																					// active
																					// records
			isEligibleForUpload = checkEligibilityForUpload(projectFileList, fileLength);
			if (!isEligibleForUpload) {
				throw new AppException(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
						AppErrorCodeEnum.FILE_SIZE_EXCEEDED.getErrorCode(), "File Size Exceeded");
			}
			if (modeEnabled != null && modeEnabled.equals("local")) {
				uploadService = new LocalUploadUtility();
				filesByUserEntity = uploadService.loadFile(multipartFormDataInput, "datafile", userId, fileService,
						fileLength);
			} else {
				uploadService = new GoogleUploadUtility();
				filesByUserEntity = uploadService.loadFile(multipartFormDataInput, "datafile", userId, fileService,
						fileLength);
			}

			if (filesByUserEntity != null && filesByUserEntity.getFileId() != null) {
				OVERALL_RESPONSE = Constants.SUCCESS;
			}
			if (createProject == true) // download the results to report
			{
				LOGGER.info("file upload service reading data from security context");
				UserInfo userInfo = (UserInfo) securityContext.getUserPrincipal();
				if (userInfo == null || userInfo.getUserId() == null) {
					LOGGER.error("file upload service  issue with authorization");
					
				}
				UserSession userSession =null;
				try
				{
					LOGGER.info("file upload service reading data from security context.headers.getRequestHeaders="+headers.getRequestHeaders());
					LOGGER.info("file upload service reading data from security context.headers.headers.getRequestHeader(Authorization)="+headers.getRequestHeader("Authorization"));
					//String sessionId = headers.getRequestHeader("Authorization").get(0);
					if( (!sessionIdParam.equalsIgnoreCase("sessionId")) )
					{
						userSession = getUserSession(sessionIdParam);
					}
					LOGGER.info("file upload service get the usersession complete");
				}
				catch(Exception e)
				{
					LOGGER.error("file upload service  unable to get the usersession");
				}
				LOGGER.info("file upload service reading data from security context.userInfo="+userInfo);
				/* For inserting into process table */
				LOGGER.info(
						"File upload service start createProcessEntityInNotStartedStatus(). Inputs  fileEntity.getFileId():"
								+ filesByUserEntity.getFileId().toString());
				List<ProcessEntity> createProcessEntityInNotStartedStatusList = projectService
						.createProcessEntityInNotStartedStatus(filesByUserEntity.getFileId().toString(), userId,
								filesByUserEntity, userInfo,userSession);
				for (ProcessEntity createProcessEntityInNotStartedStatus : createProcessEntityInNotStartedStatusList) {
					LOGGER.info("File upload service End. OutputscreatedProcessId:"
							+ createProcessEntityInNotStartedStatus.getProcessId());
					if (createProcessEntityInNotStartedStatus == null
							|| createProcessEntityInNotStartedStatus.getProcessId() == null) {
						OVERALL_RESPONSE = Constants.FAIL; // Reset
															// to
															// Fail
					} else {
						processIDs = processIDs + " " + createProcessEntityInNotStartedStatus.getProcessId();
						LOGGER.info("File upload service End. create project.processIDs=" + processIDs);
						OVERALL_RESPONSE = Constants.SUCCESS;
					}
				}
			} else {
				LOGGER.info("File upload service End. create project skipped.createProjectFlag=" + createProject);
			}
			result.add(OVERALL_RESPONSE);

		} catch (AppException e) {
			LOGGER.error("File upload service AppException failed" + e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			throw new AppException(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
					AppErrorCodeEnum.INTERNAL_SERVER_ERROR.getErrorCode(), "Internal Server Error");
		}
		if (detailsFlag == true) // return file details
		{
			return Response.ok(filesByUserEntity).build();
		} else {
			return Response.ok(result).build();
		}

	}
	
	

	public FilesByUserEntity createFilesByUserEntity(String fileParameterName, String userId,
			int fileLength, String filePath) {
		String fileName = null;
		FilesByUserEntity fileEntity = null;
		try {
			LOGGER.info("length of file uploaded is " + fileLength);
			fileName = StringUtils.substringAfterLast(filePath, "/");
			if (null != fileName && !"".equalsIgnoreCase(fileName)) {
				long fileSizeInTotal = 0;
				// long fileSizeInTotal = writeToFileServer(inputStream,
				// fileName);
				// localPathDirectory=filePath;
				fileEntity = new FilesByUserEntity();
				fileEntity.setFileId(UUID.randomUUID());
				fileEntity.setFileUpdateDate(new Date());
				fileEntity.setFileUploadBy("test");
				fileEntity.setFileUploadByUserId(UUID.fromString(userId));
				fileEntity.setRowStatus(true);
				fileEntity.setUserId(UUID.fromString(userId));
				fileEntity.setFileInitialName(fileName);
				fileEntity.setFileRandomlizedName(fileName);
				fileEntity.setFileSize(fileSizeInTotal);
				fileEntity.setFileSuffix(fileName);
				fileEntity.setPathDirectory(filePath);

				LOGGER.info("Persisiting File Detail " + fileEntity.toString());
				fileService.persistFileMetaInformation(fileEntity);
				LOGGER.info("Persisiting File Detail Completed");
				LOGGER.info("Persisiting Completed ");
			}

		} catch (Exception ioe) {
			LOGGER.info("Exception Persisiting Completed ");
			LOGGER.error("error saving file " + ioe, ioe.getMessage());
			ioe.printStackTrace();

		} 
		LOGGER.info("Completed creating file object " + fileEntity.getFileInitialName());
		return fileEntity;
	}
	
	private String getFileName(MultivaluedMap<String, String> multivaluedMap) {
		 
        String[] contentDisposition = multivaluedMap.getFirst("Content-Disposition").split(";");
 
        for (String filename : contentDisposition) {
 
            if ((filename.trim().startsWith("filename"))) {
                String[] name = filename.split("=");
                String exactFileName = name[1].trim().replaceAll("\"", "");
                return exactFileName;
            }
        }
        return "UnknownFile";
    }		
		

	private boolean checkEligibilityForUpload(List<FilesByUserEntity> projectFileList, long sizeOfNewFile) {
		long sizeOfFiles = 0;
		boolean isElgible = true;
		for (FilesByUserEntity filesByUserEntity : projectFileList) {
			sizeOfFiles = sizeOfFiles + filesByUserEntity.getFileSize();
		}
		String allowedFileSizeInString = PropertiesSingletonBean.getProperty("microbiome_allowed_file_size");
		if (allowedFileSizeInString != null && allowedFileSizeInString != "") {
			long allowedFileSizeLimitInLong = Long.valueOf(allowedFileSizeInString);
			sizeOfFiles = sizeOfFiles + sizeOfNewFile;
			if (allowedFileSizeLimitInLong > sizeOfFiles) {
				isElgible = true;
			} else {
				isElgible = false;
			}
		}
		return isElgible;
	}
	private double calculateFileSizeCapacityAvl(List<FilesByUserEntity> filesByUserEntityList) {
		LOGGER.info("calculateFileSizeCapacityAvl started ");
		double currentfileCapacityForUser = 0;
		double  fileSizeCapacityAvl = 0;
		for (FilesByUserEntity filesByUserEntity : filesByUserEntityList) {
			currentfileCapacityForUser = currentfileCapacityForUser + (double)filesByUserEntity.getFileSize();
		}
		String allowedFileSizeInString = PropertiesSingletonBean.getProperty("microbiome_allowed_file_size");
		
		
		
		if (allowedFileSizeInString != null && allowedFileSizeInString != "") {
			
			fileSizeCapacityAvl=Double.valueOf(allowedFileSizeInString)-currentfileCapacityForUser;
			
//			double allowedFileSizeLimitInLong = Double.valueOf(allowedFileSizeInString);
			
			
//			fileSizeCapacityAvl=(currentfileCapacityForUser/allowedFileSizeLimitInLong);
//			fileSizeCapacityAvl=fileSizeCapacityAvl*10000;
		}
		LOGGER.info("calculateFileSizeCapacityAvl Completed ");
		return fileSizeCapacityAvl;
	
	}


//	
//	
//	@POST
//	@Path("/v1/files")
//	@Produces("application/json")
//	public Response addFile(FilesEntityByUserJSON fileEntityJSON) throws AppException {
//		LOGGER.info("Invoking addFile Method");
//		FilesByUserEntity fileEntity = null;
//		try {
//
//			if (fileEntityJSON == null) {
//				throw new AppException(javax.ws.rs.core.Response.Status.BAD_REQUEST.getStatusCode(),
//						AppErrorCodeEnum.BAD_REQUEST.getErrorCode(), "One of the request inputs is not valid");
//			}
//			validateJSON(fileEntityJSON);
//
//			fileEntity = new FilesByUserEntity();
//
//			fileEntity.setFileId(UUID.randomUUID());
//			fileEntity.setFileUpdateDate(new Date());
//			fileEntity.setFileUploadBy(fileEntityJSON.getFileUploadedBy());
//			fileEntity.setFileUploadByUserId(UUID.fromString(fileEntityJSON.getFileUploadedBy()));
//		    fileEntity.setRowStatus(true);
//			fileEntity.setUserId(UUID.fromString(fileEntityJSON.getUserId()));
//			fileEntity.setFileInitialName(fileEntityJSON.getFileInitialName());
//			fileEntity.setFileRandomlizedName(fileEntityJSON.getFileRandomlizedName());
//			fileEntity.setFileSize(Long.valueOf(fileEntityJSON.getFileSize()));
//			fileEntity.setFileSuffix(fileEntityJSON.getFileSuffix());
//			fileEntity.setPathDirectory(fileEntityJSON.getPathDirectory());
//			fileEntity.setProcessType(fileEntityJSON.getProcessType());
//			fileService.persistFileMetaInformation(fileEntity);
//
//			
//		} catch (Exception e) {
//			throw new AppException(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
//					AppErrorCodeEnum.INTERNAL_SERVER_ERROR.getErrorCode(), "Internal Server Error");
//		}
//
//		List<String> responseData = new ArrayList<String>();
//		responseData.add(fileEntity.getFileId().toString());
//		ResponseList responseList = new ResponseList();
//		responseList.setList(responseData);
//
//		return Response.ok(responseList).build();
//	
//
//	}

	@GET
	@Path("/v1/files")
	@Produces("application/json")
	public Response fetchFileMetaByUserId(@QueryParam("userId") UUID userId) throws AppException {

		List<FilesByUserEntity> projectFileEntities;
		try {
			LOGGER.info("fetchFileMeta ");
			if (userId == null) {
				throw new AppException(javax.ws.rs.core.Response.Status.BAD_REQUEST.getStatusCode(),
						AppErrorCodeEnum.BAD_REQUEST.getErrorCode(), "Invalid Request Input");
			} else {
				projectFileEntities = fileService.fetchByUserId(userId);
				LOGGER.info("fetch file size before sample " + projectFileEntities.size());
				projectFileEntities.add(0, sampleServiceData.getSampleFilesByUserId(userId));
				LOGGER.info("fetch file size after sample " + projectFileEntities.size());
			}
		}
			catch (Exception e) {
			throw new AppException(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
					AppErrorCodeEnum.INTERNAL_SERVER_ERROR.getErrorCode(), "Internal Server Error");

		}

		ResponseList responseList = new ResponseList();
		responseList.setList(projectFileEntities);

		return Response.ok(responseList).build();
	}

	@GET
	@Path("/v1/files/{fileId}")
	@Produces("application/json")
	public Response fetchFileMetaByFileId(@PathParam("fileId") UUID fileId) throws AppException {

		List<FilesByUserEntity> projectFileEntities;
		try {
			LOGGER.info("fetchFileMetaByFileId " + fileId);
			if (fileId == null) {
				throw new AppException(javax.ws.rs.core.Response.Status.BAD_REQUEST.getStatusCode(),
						AppErrorCodeEnum.BAD_REQUEST.getErrorCode(), "Invalid Request Input");
			} else {
				projectFileEntities = fileService.fetchByFileId(fileId);
			}
		}
			catch (Exception e) {
			throw new AppException(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
					AppErrorCodeEnum.INTERNAL_SERVER_ERROR.getErrorCode(), "Internal Server Error");

		}

		ResponseList responseList = new ResponseList();
		responseList.setList(projectFileEntities);

		return Response.ok(responseList).build();
	}
	@GET
	@Path("/v1/files/filesizecapacity/{userId}")
	@Produces("application/json")
	public Response fetchFileSizeCapacity(@PathParam("userId") UUID userId) throws AppException {

		FileSizeCapacityJSON fileSizeCapacityJSON = new FileSizeCapacityJSON();
		List<FilesByUserEntity> projectFileList;
		try {
			LOGGER.info("fetchFileSizeCapacity " + userId);
			if (userId == null) {
				throw new AppException(javax.ws.rs.core.Response.Status.BAD_REQUEST.getStatusCode(),
						AppErrorCodeEnum.BAD_REQUEST.getErrorCode(), "Invalid Request Input");
			} else {
				projectFileList = fileService.fetchByUserId(userId);// Fetch
																	// active
																	// records
				if (projectFileList == null) {
					String maxFileSize = PropertiesSingletonBean.getProperty("microbiome_allowed_file_size");
					fileSizeCapacityJSON.setFreeSpace(Long.valueOf(maxFileSize));
					fileSizeCapacityJSON.setUsedSpace(0L);
				} else {
					fileSizeCapacityJSON = fileService.calculateFileSizeCapacityAvl(projectFileList);
				}
				fileSizeCapacityJSON.setUserId(userId);

			}
		} catch (Exception e) {
			throw new AppException(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
					AppErrorCodeEnum.INTERNAL_SERVER_ERROR.getErrorCode(), "Internal Server Error");

		}
		return Response.ok(fileSizeCapacityJSON).build();
	}
	
	private Boolean validateJSONAndFileExtn(MultipartFormDataInput multipartFormDataInput) throws AppException {
		/***validation of inputs **/
		Boolean returnFlag=false;
		MultivaluedMap<String, String> multivaluedMap = null;
		Map<String, List<InputPart>> map = multipartFormDataInput.getFormDataMap();
		List<InputPart> lstInputPart = map.get("datafile");
		String fileName = null;
		if(lstInputPart==null)	//if input file is selected
		{
			throw new AppException(javax.ws.rs.core.Response.Status.BAD_REQUEST.getStatusCode(),
					AppErrorCodeEnum.MISSING_INPUT_FILE_NAME.getErrorCode(), "Input File not found");
		}
		else
		{
			for (InputPart inputPart : lstInputPart) {
				// get filename to be uploaded
				multivaluedMap = inputPart.getHeaders();
				fileName = getFileName(multivaluedMap);
				if (null != fileName && !"".equalsIgnoreCase(fileName)) {
					returnFlag=validateFileExtension(fileName);
				}
			}
		}
		return returnFlag;
	}
	private Boolean validateFileExtension(String fileName) throws AppException {
		String fileExtn = "";
		Boolean validExtn=false;
		try {
//			int i = fileName.lastIndexOf('.');
//			if (i >= 0) {
//				fileExtn = fileName.substring(i + 1);
//			}
			validExtn= fileName.endsWith(Constants.FILE_EXTN_FA)
					|| fileName.endsWith(Constants.FILE_EXTN_FQ)
					|| fileName.endsWith(Constants.FILE_EXTN_FASTA)
					|| fileName.endsWith(Constants.FILE_EXTN_FASTQ)
					|| fileName.endsWith(Constants.FILE_EXTN_GZ)
					|| fileName.endsWith(Constants.FILE_EXTN_BZ2);
			LOGGER.info("validateFileExtension validExtn=" + validExtn  +";fileName="+fileName
					+";fileExtn="+fileExtn);
		} catch (Exception e) {
			LOGGER.error("validateFileExtension failed" + e.getMessage(), e);
			throw new AppException(javax.ws.rs.core.Response.Status.BAD_REQUEST.getStatusCode(),
					AppErrorCodeEnum.INVALID_INPUT_FILE_EXTN.getErrorCode(), "Invalid File Extension");
		
		}
		return validExtn;
	}
	@DELETE
	@Path("/v1/deletefile/{fileId}")
	@Produces("application/json")
	public Response disableFile(@PathParam("fileId") UUID fileId) throws AppException {
		LOGGER.info("disableFile " + fileId);

		ResponseList responseList = null;
		boolean result=false;
		boolean resultProcess=false;
		List<ProcessEntity> processDetailList = null;
		try {
			if (fileId == null) {
				throw new AppException(javax.ws.rs.core.Response.Status.BAD_REQUEST.getStatusCode(),
						AppErrorCodeEnum.BAD_REQUEST.getErrorCode(), "Invalid Request Input");
			}
			if (fileId.equals(UUID.fromString(Constants.DEFAULT_FILE_ID))) {
				throw new AppException(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
						AppErrorCodeEnum.INTERNAL_SERVER_ERROR.getErrorCode(), "SAMPLE FILE CANNOT BE REMOVED");
			}
			processDetailList = projectService.fetchByFileId(fileId,"ALL");
			Boolean isProcessAssociatedWithAnalysis = false;
			Boolean isResultsAssociatedToProcessDeleted = false;
			LOGGER.info("deletefile List of process fileprocessDetailList=" + processDetailList);
			if (processDetailList != null && !processDetailList.isEmpty()) {
				// check for linked MSA process
				for (ProcessEntity processEntity : processDetailList) {
						if(Constants.PROCESS_STATUS.InProgress.status().equalsIgnoreCase(processEntity.getProcessStatus())
								||Constants.PROCESS_STATUS.PENDING.status().equalsIgnoreCase(processEntity.getProcessStatus()))
						{
							throw new AppException(javax.ws.rs.core.Response.Status.BAD_REQUEST.getStatusCode(),
									AppErrorCodeEnum.BAD_REQUEST.getErrorCode(), "File can't be deleted as the linked Processes is in InProgress or Pending Status");
						}
					 
					List<AnalysisEntity> analysisByUserIdAndProcessId = analysisService
							.getAnalysisByUserIdAndProcessId(processEntity.getAddedBy(), processEntity.getProcessId());
					if (!analysisByUserIdAndProcessId.isEmpty()) {
						isProcessAssociatedWithAnalysis = true;
					}
				}

				if (!isProcessAssociatedWithAnalysis) {
					for (ProcessEntity processEntity : processDetailList) {
						/* delete the meta results table if exists */
						List<MetaResultEntity> metaResultList;
						metaResultList = metaService.getAllMetaResultsForProcessId(processEntity.getProcessId());
						if (!metaResultList.isEmpty()) {
							isResultsAssociatedToProcessDeleted = metaService
									.markAsDeleteForAllMetaResultResultsByProcessIdAndType(metaResultList);
							LOGGER.info("deletefile deleted Meta results for isResultsAssociatedToProcessDeleted="
									+ isResultsAssociatedToProcessDeleted);
						}
						else
						{
							isResultsAssociatedToProcessDeleted=true;
						}

						resultProcess = projectService.removeByProcessId(processEntity.getProcessId());
						LOGGER.info("deletefile Meta results of process ProcessId()=" + processEntity.getProcessId()
								+ "marked as inactive");
					}
				}
			}
			if (!isProcessAssociatedWithAnalysis) {
				result = fileService.removeFileByFileId(fileId);
				LOGGER.info("deletefile List of process fileId=" + fileId + "marked as inactive");
			} else {
				throw new AppException(javax.ws.rs.core.Response.Status.BAD_REQUEST.getStatusCode(),
						AppErrorCodeEnum.FILE_DELETION_MSA_LINK_EXISTS.getErrorCode(),
						"Unable to delete file as associated processes are linked to MultiSampleAnalysis. Plz check");
			}
			LOGGER.info("deletefile service completed successfully");
			if (result == true && resultProcess == true && isResultsAssociatedToProcessDeleted==true) {
				List<String> responseData = new ArrayList<String>();
				responseData.add(fileId.toString());
				responseList = new ResponseList();
				responseList.setList(responseData);
			} else {
				throw new AppException(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
						AppErrorCodeEnum.INTERNAL_SERVER_ERROR.getErrorCode(), "Internal Server Error");

			}
		} 
		catch (AppException e) {
			throw e;
		} 
		catch (Exception e) {
			throw new AppException(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
					AppErrorCodeEnum.INTERNAL_SERVER_ERROR.getErrorCode(), "Internal Server Error");
		}
		return Response.ok(responseList).build();

	}

	@GET
	@Path("/v1/checkeligiblefile/{fileId}")
	@Produces("application/json")
	public Response checkFileEligible(@PathParam("fileId") String fileId) throws AppException {

		LOGGER.info("checkFileEligible for fileId " + fileId);

		if (fileId == null) {
			throw new AppException(javax.ws.rs.core.Response.Status.BAD_REQUEST.getStatusCode(),
					AppErrorCodeEnum.BAD_REQUEST.getErrorCode(), "Invalid Request Input");
		}
		if (fileId.equals(Constants.DEFAULT_FILE_ID)) {
			LOGGER.info("Default File Id Cannot be deleted");
			throw new AppException(javax.ws.rs.core.Response.Status.BAD_REQUEST.getStatusCode(),
					AppErrorCodeEnum.SAMPLE_FILE_CANNOT_BE_DELETED.getErrorCode(), "Sample File Cannot Be Deleted");
		}

		boolean result = fileService.checkEligibleFile(UUID.fromString(fileId));
		List<String> responseList = new ArrayList<String>();
		if (result) {
			responseList.add("true");
		}
		return Response.ok(responseList).build();

	}
	
	@DELETE
	@Path("/v1/deleteeligiblefile/{fileId}")
	@Produces("application/json")
	public Response deleteEligibleFile(@PathParam("fileId") String fileId) throws AppException {

		LOGGER.info("deleteEligibleFile for fileId " + fileId);

		if (fileId == null) {
			throw new AppException(javax.ws.rs.core.Response.Status.BAD_REQUEST.getStatusCode(),
					AppErrorCodeEnum.BAD_REQUEST.getErrorCode(), "Invalid Request Input");
		}
		if (fileId.equals(Constants.DEFAULT_FILE_ID)) {
			throw new AppException(javax.ws.rs.core.Response.Status.BAD_REQUEST.getStatusCode(),
					AppErrorCodeEnum.SAMPLE_FILE_CANNOT_BE_DELETED.getErrorCode(), "Sample File Cannot Be Deleted");
		}

		boolean result = fileService.deleteEligibleFile(UUID.fromString(fileId));
		List<String> responseList = new ArrayList<String>();
		if (result) {
			responseList.add("true");
		}
		return Response.ok(responseList).build();
	}
	
	@POST
	@Path("/v1/files/batchupload/{userid}")
	@Consumes("multipart/form-data")
	@Produces("application/json")
	public Response uploadFileList(@HeaderParam("Content-Length") int fileLength, MultipartFormDataInput multipartFormDataInput,
			@PathParam(value = "userid") String userId,
			@QueryParam("createProjectFlag") @DefaultValue("true") Boolean createProject,
			@QueryParam("detailsFlag") @DefaultValue("false") Boolean detailsFlag) throws AppException {

//		boolean uploadStatus = false;
		List<String> result = new ArrayList<String>();
		String OVERALL_RESPONSE = Constants.FAIL;
		String processIDs = "";
		FilesByUserEntity filesByUserEntity = null;
		List<FileEntityWithProcessListWrapper> fileEntityWithProcessListWrapperList = new ArrayList<FileEntityWithProcessListWrapper>();
		try {
//				String modeEnabled = PropertiesSingletonBean.getProperty(MicrobiomeToolConstants.MODE_DEBUG);
				Map<String, List<InputPart>> map = multipartFormDataInput.getFormDataMap();
				List<InputPart> lstInputPart = map.get("datafile");
				
				InputStream inputStream = null;
				 String str = "";
			        StringBuffer buf = new StringBuffer();    
				MultivaluedMap<String, String> multivaluedMap = null;
				 String fileName = null;
				for (InputPart inputPart : lstInputPart) {
		                    inputStream = inputPart.getBody(InputStream.class,null);
		                    
		                    try {
		                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		                        List<ProcessEntity> createProcessEntityInNotStartedStatusList = null;
		                        if (inputStream != null) {                            
		                            while ((str = reader.readLine()) != null) {    
		                                buf.append(str + "\n" );
		                                LOGGER.info("buf=" +buf);
		                                filesByUserEntity = createFilesByUserEntity("datafile", userId, fileService,
												fileLength,str);
		                            	UserInfo userInfo = (UserInfo) securityContext.getUserPrincipal();
		                				if (userInfo == null || userInfo.getUserId() == null) {
		                					LOGGER.error("issue with authorization");
		                					
		                				}
										LOGGER.info(
												"File upload service start createProcessEntityInNotStartedStatus(). Inputs  fileEntity:"
														+ filesByUserEntity);
											createProcessEntityInNotStartedStatusList = projectService
													.createProcessEntityInNotStartedStatus(filesByUserEntity.getFileId().toString(),
															userId, filesByUserEntity,userInfo,null);
											for (ProcessEntity createProcessEntityInNotStartedStatus : createProcessEntityInNotStartedStatusList) {
												LOGGER.info("File upload service End. OutputscreatedProcessId:"
														+ createProcessEntityInNotStartedStatus.getProcessId());
												UUID vProcessId = createProcessEntityInNotStartedStatus.getProcessId();
												if (createProcessEntityInNotStartedStatus == null
														|| createProcessEntityInNotStartedStatus.getProcessId() == null) {
													OVERALL_RESPONSE = Constants.FAIL; // Reset
																										// to
																										// Fail
												} else {
													processIDs = processIDs + " "
															+ createProcessEntityInNotStartedStatus.getProcessId();
													LOGGER.info("File upload service End. create project.processIDs=" + processIDs);
													/*writing to Message Queue*/
													OVERALL_RESPONSE = Constants.SUCCESS;
												}
											}
											fileEntityWithProcessListWrapperList.add(new FileEntityWithProcessListWrapper(filesByUserEntity,createProcessEntityInNotStartedStatusList));
		                            }                
		                        }
		                    } finally {
		                        try { inputStream.close(); } catch (Throwable ignore) {}
		                    }
				}
					result.add(OVERALL_RESPONSE);
			
		} catch(AppException e) {
			if(e.getCode()==AppErrorCodeEnum.MISSING_INPUT_FILE_NAME.getErrorCode())
			{
				throw new AppException(javax.ws.rs.core.Response.Status.OK.getStatusCode(),
						AppErrorCodeEnum.MISSING_INPUT_FILE_NAME.getErrorCode(), "Invalid Inputs. Plz check the input parameters as File, param name etc");
			}
			else
			{
				throw new AppException(javax.ws.rs.core.Response.Status.OK.getStatusCode(),
						AppErrorCodeEnum.FILE_SIZE_EXCEEDED.getErrorCode(), "File Size Exceeded");
				
			}
		}
		catch (Exception e) {
			throw new AppException(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
					AppErrorCodeEnum.INTERNAL_SERVER_ERROR.getErrorCode(), "Internal Server Error");
		}
		if (detailsFlag == true) // return file details
		{
			return Response.ok(fileEntityWithProcessListWrapperList).build();
		}
		else
		{
			return Response.ok(result).build();
		}

	}
	public FilesByUserEntity createFilesByUserEntity(String fileParameterName, String userId,FileService fileService,int fileLength,String filePath) {
		String fileName = null;
		FilesByUserEntity fileEntity = null;
		InputStream inputStream = null;
		try {
			LOGGER.info("length of file uploaded is " + fileLength);
				fileName=StringUtils.substringAfterLast(filePath,"/");
					if (null != fileName && !"".equalsIgnoreCase(fileName)) {
						long fileSizeInTotal =0;
//						long fileSizeInTotal = writeToFileServer(inputStream, fileName);
//						localPathDirectory=filePath;
						fileEntity = new FilesByUserEntity();
						fileEntity.setFileId(UUID.randomUUID());
						fileEntity.setFileUpdateDate(new Date());
						fileEntity.setFileUploadBy("test");
						fileEntity.setFileUploadByUserId(UUID.fromString(userId));
						fileEntity.setRowStatus(true);
						fileEntity.setUserId(UUID.fromString(userId));
						fileEntity.setFileInitialName(fileName);
						fileEntity.setFileRandomlizedName(fileName);
						fileEntity.setFileSize(fileSizeInTotal);
						fileEntity.setFileSuffix(fileName);
						fileEntity.setPathDirectory(filePath);
						LOGGER.info("Persisiting File Detail " + fileEntity.toString());
						fileService.persistFileMetaInformation(fileEntity);
						LOGGER.info("Persisiting Completed ");
					}
			return fileEntity;
		} catch (Exception ioe) {
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {

				}
			}
		}
		return fileEntity;
	}
	public UserSession getUserSession(String sessionIdFromRequest)
			throws AppException, IOException, RemoteException {

		LOGGER.info("getUserSession sessionIdFromRequest:" + sessionIdFromRequest);

		String userModuleBaseUrl = PropertiesSingletonBean.getProperty("USER_MODULE_BASE_URL");
		String url = userModuleBaseUrl + "/userserv/v1/users/me";

		LOGGER.info("user authentication url:" + url);
		//Map<String, String> queryMap = new HashMap<String, String>();
		//queryMap.put("Authorization", sessionIdFromRequest);
		//Response response = restClient.restGetByQueryParam(url, queryMap);
		Response response = restClient.restGet(url, sessionIdFromRequest);
		LOGGER.info("user authentication received response " + response);
		
		UserSession userSessionJSON = new UserSession();
		LOGGER.info("user authentication status :" + response.getStatus());
		if (response.getStatus() == 200) {

			final ObjectMapper mapper = new ObjectMapper();
			try {
				userSessionJSON = mapper.readValue(response.readEntity(String.class),
						mapper.getTypeFactory().constructType(UserSession.class));
			} catch (final IOException e) {
				e.printStackTrace();
			}

			// UserSession userSessionJSON =
			// response.readEntity(UserSession.class);

			if (userSessionJSON == null) {
				throw new NotAuthorizedException("Oops... Not Authorized!");
			}
			final UUID userId = userSessionJSON.getUserId();
			final String logonId = userSessionJSON.getLogonId();

			LOGGER.info("userSessionJSON userId:" + userId);
			LOGGER.info("userSessionJSON firstName:" + userSessionJSON.getFirstName());
			LOGGER.info("userSessionJSON lastName:" + userSessionJSON.getLastName());
			LOGGER.info("userSessionJSON email:" + userSessionJSON.getEmail());
			
		}
		return userSessionJSON;
	}
}