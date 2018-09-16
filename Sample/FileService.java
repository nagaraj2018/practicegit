package com.training.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import javax.annotation.ManagedBean;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.training.dao.ProjectFileDaoBean;
import com.training.entity.AnalysisEntity;
import com.training.entity.FilesByUserEntity;
import com.training.entity.MetaResultEntity;
import com.training.entity.ProcessEntity;
import com.training.exception.AppErrorCodeEnum;
import com.training.exception.AppException;
import com.training.exception.DBRemoteException;
import com.training.model.FileSizeCapacityJSON;
import com.training.utility.Constants;
import com.training.utility.PropertiesSingletonBean;
import com.training.utility.RestDelegate;

@ManagedBean
public class FileService {

	private static final Logger LOGGER = LogManager.getLogger(FileService.class.getName());

	@Inject
	ProjectFileDaoBean projectFileDaoBean;

	@Inject
	ProcessService projectService;

	@Inject
	AnalysisService analysisService;

	@Inject
	MetaService metaService;

	@Inject
	RestDelegate restDelegate;

	public List<FilesByUserEntity> fetchByUserId(UUID userId) {
		LOGGER.info("start fetchByUserId");
		List<FilesByUserEntity> filesByUserId = null;
		filesByUserId = projectFileDaoBean.getFilesByUserId(userId);
		// added sorting logic based on update date time
		LOGGER.info("start fetchByUserId() sort functionality fetchByUserId");
		if (filesByUserId != null && !filesByUserId.isEmpty()) {
			Collections.sort(filesByUserId, filesByUserEntityBasedOnUpdateDt);
		}
		LOGGER.info("end fetchByUserId() sort functionality fetchByUserId");
		return filesByUserId;

	}

	public static Comparator<FilesByUserEntity> filesByUserEntityBasedOnUpdateDt = new Comparator<FilesByUserEntity>() {

		@Override
		public int compare(FilesByUserEntity d1, FilesByUserEntity d2) {
			if (null == d1 || null == d2 || null == d1.getFileUpdateDate() || null == d1.getFileUpdateDate()) {
				throw new NullPointerException();
			} else {
				return d2.getFileUpdateDate().compareTo(d1.getFileUpdateDate());
			}
		}
	};

	public List<FilesByUserEntity> fetchByFileId(UUID fileId) {

		return projectFileDaoBean.getFilesByFileId(fileId);

	}

	public boolean persistFileMetaInformation(FilesByUserEntity entity) {
		LOGGER.info("persist file data");
		boolean persistResult = false;
		try {
			persistResult = projectFileDaoBean.saveFile(entity);
			persistResult = true;

		} catch (Exception e) {
			LOGGER.error("Error updateAnalysis" + e.getMessage(), e);
			throw new DBRemoteException("persistFileMetaInformation failed");
		}
		LOGGER.info("persist file data completed" + persistResult);
		return persistResult;
	}

	public boolean removeFileByFileId(UUID fileId) {
		List<FilesByUserEntity> filesByUserEntityList = fetchByFileId(fileId);
		if (filesByUserEntityList != null && filesByUserEntityList.size() > 0) {
			FilesByUserEntity fileToUpdate = filesByUserEntityList.get(0);
			fileToUpdate.setRowStatus(false);
			return projectFileDaoBean.updateFile(fileToUpdate);
		} else {
			LOGGER.info("No file found for file id " + fileId);
			return true;
		}
	}

	public FileSizeCapacityJSON calculateFileSizeCapacityAvl(List<FilesByUserEntity> filesByUserEntityList)
			throws AppException {
		LOGGER.info("calculateFileSizeCapacityAvl started ");
		double currentFileSize = 0;
		double avlFileSize = 0;
		FileSizeCapacityJSON fileSizeCapacityJSON = new FileSizeCapacityJSON();

		String maxFileSize = PropertiesSingletonBean.getProperty("microbiome_allowed_file_size");

		if (maxFileSize == null) {
			throw new AppException(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
					AppErrorCodeEnum.INTERNAL_SERVER_ERROR.getErrorCode(), "Max File Size Not Configured");
		}
		for (FilesByUserEntity filesByUserEntity : filesByUserEntityList) {
			currentFileSize = currentFileSize + (double) filesByUserEntity.getFileSize();
		}

		if (maxFileSize != null && maxFileSize != "") {

			avlFileSize = Double.valueOf(maxFileSize) - currentFileSize;
		}

		if (avlFileSize >= Double.valueOf(maxFileSize)) {
			fileSizeCapacityJSON.setFreeSpace(Long.valueOf(maxFileSize));
			fileSizeCapacityJSON.setUsedSpace(0L);
		} else {

			if (avlFileSize > 0) {
				fileSizeCapacityJSON.setFreeSpace(Math.abs(Math.round(avlFileSize)));
				fileSizeCapacityJSON.setUsedSpace(Math.abs(Math.round(Double.valueOf(maxFileSize) - avlFileSize))); // value
																													// in
																													// %
			} else {
				fileSizeCapacityJSON.setFreeSpace(0L);
				fileSizeCapacityJSON.setUsedSpace(Long.valueOf(maxFileSize));
			}
		}
		LOGGER.info("calculateFileSizeCapacityAvl Completed ");
		return fileSizeCapacityJSON;

	}

	public boolean checkEligibleFile(UUID fileId) throws AppException {

		List<ProcessEntity> activeProcessListAssociatedToFile = null;
		List<FilesByUserEntity> filesByUserEntities = null;
		try {
			
			activeProcessListAssociatedToFile = projectService.fetchByFileId(fileId,"ALL");
			filesByUserEntities = projectFileDaoBean.getFilesByFileId(fileId);
			if (activeProcessListAssociatedToFile != null && !activeProcessListAssociatedToFile.isEmpty()) {

				long pendingProcessCount = activeProcessListAssociatedToFile.stream().filter((process) -> process
						.getProcessStatus().equalsIgnoreCase(Constants.PROCESS_STATUS.InProgress.status())
						|| process.getProcessStatus().equalsIgnoreCase(Constants.PROCESS_STATUS.PENDING.status()))
						.count();

				if (pendingProcessCount > 0) {
					LOGGER.info(pendingProcessCount + "Process are in pending state for fileId " + fileId);
					throw new AppException(javax.ws.rs.core.Response.Status.BAD_REQUEST.getStatusCode(),
							AppErrorCodeEnum.PROCESS_ARE_IN_PENDING.getErrorCode(), "Process are in pending state for " +filesByUserEntities.get(0).getFileInitialName() + "." + filesByUserEntities.get(0).getFileInitialName() +  " cannot be deleted");
				} else {

					for (ProcessEntity activeAssociatedProcess : activeProcessListAssociatedToFile) {
						List<AnalysisEntity> analysisAssociatedToProcess = analysisService
								.getAnalysisByUserIdAndProcessId(activeAssociatedProcess.getAddedBy(),
										activeAssociatedProcess.getProcessId());
						if (analysisAssociatedToProcess != null && !analysisAssociatedToProcess.isEmpty()) {
							long pendingAnalysisCount = analysisAssociatedToProcess.stream()
									.filter((analysis) -> analysis.getAnalysisStatus()
											.equalsIgnoreCase(Constants.PROCESS_STATUS.InProgress.status())
											|| analysis.getAnalysisStatus()
													.equalsIgnoreCase(Constants.PROCESS_STATUS.PENDING.status()))
									.count();
							if (pendingAnalysisCount > 0) {
								LOGGER.info(pendingAnalysisCount
										+ "Analysis of Process are in pending state for fileId " + fileId);
								throw new AppException(javax.ws.rs.core.Response.Status.BAD_REQUEST.getStatusCode(),
										AppErrorCodeEnum.ANALYSIS_ARE_IN_PENDING.getErrorCode(),
										"Analysis of Process are in pending state for "+filesByUserEntities.get(0).getFileInitialName() + "." + filesByUserEntities.get(0).getFileInitialName() +  " cannot be deleted");
							}

						}
					}
				}
			}

			else {
				LOGGER.info("File " + fileId + " is eligible for delete ");
				return true;
			}
		} catch (Exception e) {
			throw e;
		}
		LOGGER.info("File " + fileId + " is eligible for delete ");
		return true;
	}

	public boolean deleteEligibleFile(UUID fileId) throws AppException {

		boolean checkEligibleFlag;
		List<FilesByUserEntity> filesByUserEntities = null;
		try {
			checkEligibleFlag = checkEligibleFile(fileId);
		} catch (Exception e) {
			throw e;
		}
		if (checkEligibleFlag) {
			filesByUserEntities = projectFileDaoBean.getFilesByFileId(fileId);
			List<ProcessEntity> activeProcessListBucket = projectService.fetchByFileId(fileId,"ALL");
			List<AnalysisEntity> activeAnalysisListBucket = new ArrayList<AnalysisEntity>();
			try {

				for (ProcessEntity activeProcess : activeProcessListBucket) {
					List<AnalysisEntity> analysisAssociatedToProcess = analysisService
							.getAnalysisByUserIdAndProcessId(activeProcess.getAddedBy(), activeProcess.getProcessId());
					if (analysisAssociatedToProcess != null && !analysisAssociatedToProcess.isEmpty()) {
						for (AnalysisEntity analysisEntity : analysisAssociatedToProcess) {
							activeAnalysisListBucket.add(analysisEntity);
						}

					}
				}

				LOGGER.info("File ---> Process " + activeProcessListBucket.size());
				LOGGER.info("File ---> Analysis " + activeAnalysisListBucket.size());

				for (ProcessEntity deleteProcessId : activeProcessListBucket) {
					List<MetaResultEntity> metaResultList = metaService
							.getAllMetaResultsForProcessId(deleteProcessId.getProcessId());
					if (!metaResultList.isEmpty()) {
						metaService.markAsDeleteForAllMetaResultResultsByProcessIdAndType(metaResultList);
					}
					projectService.removeByProcessId(deleteProcessId.getProcessId());
					deletePipeLineId(deleteProcessId.getProcessId());
					LOGGER.info("Deleted [Process] Id " + deleteProcessId.getProcessId());
				}
				for (AnalysisEntity deleteAnalysisId : activeAnalysisListBucket) {
					List<MetaResultEntity> metaResultList = metaService
							.getAllMetaResultsForProcessId(deleteAnalysisId.getAnalysisId());
					if (!metaResultList.isEmpty()) {
						metaService.markAsDeleteForAllMetaResultResultsByProcessIdAndType(metaResultList);
					}
					analysisService.removeByAnalysisId(deleteAnalysisId);
					deletePipeLineId(deleteAnalysisId.getAnalysisId());
					LOGGER.info("Deleted [Analysis] Id " + deleteAnalysisId.getAnalysisId());
				}
				boolean fileDeleteresult = removeFileByFileId(fileId);
				LOGGER.info("deletefile List of process fileId=" + fileId + "marked as inactive " + fileDeleteresult);

			} catch (Exception e) {
				LOGGER.error("Error Deleting " + fileId+ " exception "+e.getMessage(),e);
				throw new AppException(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
						AppErrorCodeEnum.INTERNAL_SERVER_ERROR.getErrorCode(), "Internal Server error");
			}
		} else {
			throw new AppException(javax.ws.rs.core.Response.Status.BAD_REQUEST.getStatusCode(),
					AppErrorCodeEnum.FILE_IS_HAVING_ASSOCIATION_OTHER_PROCESS.getErrorCode(), filesByUserEntities.get(0).getFileInitialName() +" is having association with other process. " +filesByUserEntities.get(0).getFileInitialName() +"  cannot be deleted");
		}
		return true;
	}

	private Boolean deletePipeLineId(UUID pipelineId) {

		String url_for_log = String.format(PropertiesSingletonBean.getProperty(Constants.PIPELINE_BASE_URL_KEY)
				+ PropertiesSingletonBean.getProperty(Constants.DELETE_PROCESS_PIPELINE), pipelineId);
		boolean invokeStatus = false;
		LOGGER.info("delete process data from url " + url_for_log);
		Response response = null;
		try {
			response = restDelegate.restDelete(url_for_log, null);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			LOGGER.error("deletePipeLineId did not delete" + pipelineId);
		}
		if ((response == null) || (response.getStatus() != 200)) {
			invokeStatus = false;
			LOGGER.error("failed delete pipeline process " + pipelineId);

		} else {
			invokeStatus = true;
		}
		return invokeStatus;
	}

	

}

