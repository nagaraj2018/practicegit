/**
 * 
 */
package com.training.entity;

import java.util.Date;
import java.util.UUID;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

/**
 * @author pjain3
 *
 */
@Table(name = "files_by_user")
public class FilesByUserEntity  {

	@PartitionKey(0)
	@Column(name = "user_id")
	private UUID userId;
	
	@ClusteringColumn(0)
	@Column(name = "file_id")
	private UUID fileId;
	

	@Column(name = "file_inital_name")
	private String fileInitialName;
	
	@Column(name = "file_randomlized_name")
	private String fileRandomlizedName;
	
	@Column(name = "file_size")
	private long fileSize;
	
	@Column(name = "file_suffix")
	private String fileSuffix;
		
	@Column(name = "file_update_date")
	private Date fileUpdateDate;
	
	@Column(name = "file_upload_by")
	private String fileUploadBy;
	
	@Column(name = "file_upload_by_userid")
	private UUID fileUploadByUserId;
	
//	@Column(name = "process_id")
//	private UUID processId;

	@Column(name = "path_dir")
	private String pathDirectory;

//	@Column(name = "process_type")
//	private String processType;

	@Column(name = "paired_file")
	private UUID pairedFile;

	@Column(name = "active")
	private boolean rowStatus;

	

	@Column(name = "linked_process_update_date")
	private Date linkedProcessUpdatedDate;
	
	public UUID getFileId() {
		return fileId;
	}

	public void setFileId(UUID fileId) {
		this.fileId = fileId;
	}

	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}

	public String getFileInitialName() {
		return fileInitialName;
	}

	public void setFileInitialName(String fileInitialName) {
		this.fileInitialName = fileInitialName;
	}

	public String getFileRandomlizedName() {
		return fileRandomlizedName;
	}

	public void setFileRandomlizedName(String fileRandomlizedName) {
		this.fileRandomlizedName = fileRandomlizedName;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public String getFileSuffix() {
		return fileSuffix;
	}

	public void setFileSuffix(String fileSuffix) {
		this.fileSuffix = fileSuffix;
	}

	public Date getFileUpdateDate() {
		return fileUpdateDate;
	}

	public void setFileUpdateDate(Date fileUpdateDate) {
		this.fileUpdateDate = fileUpdateDate;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("FilesByUserEntity [fileId=").append(fileId).append(", userId=").append(userId)
				.append(", fileInitialName=").append(fileInitialName).append(", fileRandomlizedName=")
				.append(fileRandomlizedName).append(", fileSize=").append(fileSize).append(", fileSuffix=")
				.append(fileSuffix).append(", fileUpdateDate=").append(fileUpdateDate == null?" ":fileUpdateDate).append(", fileUploadBy=")
				.append(fileUploadBy).append(", fileUploadByUserId=").append(fileUploadByUserId)
				//.append(", processId=") .append(processId)
				.append(", pathDirectory=").append(pathDirectory)
				//.append(", processType=").append(processType)
				.append(", pairedFile=").append(pairedFile).append(", rowStatus=").append(rowStatus)
				.append(linkedProcessUpdatedDate == null?" ":linkedProcessUpdatedDate)
				.append("]");
		return builder.toString();
	}

	public String getFileUploadBy() {
		return fileUploadBy;
	}

	public void setFileUploadBy(String fileUploadBy) {
		this.fileUploadBy = fileUploadBy;
	}

	public UUID getFileUploadByUserId() {
		return fileUploadByUserId;
	}

	public void setFileUploadByUserId(UUID fileUploadByUserId) {
		this.fileUploadByUserId = fileUploadByUserId;
	}



	public String getPathDirectory() {
		return pathDirectory;
	}

	public void setPathDirectory(String pathDirectory) {
		this.pathDirectory = pathDirectory;
	}

//	public String getProcessType() {
//		return processType;
//	}
//
//	public void setProcessType(String processType) {
//		this.processType = processType;
//	}

	public UUID getPairedFile() {
		return pairedFile;
	}

	public void setPairedFile(UUID pairedFile) {
		this.pairedFile = pairedFile;
	}

	public boolean isRowStatus() {
		return rowStatus;
	}

	public void setRowStatus(boolean rowStatus) {
		this.rowStatus = rowStatus;
	}

	public Date getLinkedProcessUpdatedDate() {
		return linkedProcessUpdatedDate;
	}

	public void setLinkedProcessUpdatedDate(Date linkedProcessUpdatedDate) {
		this.linkedProcessUpdatedDate = linkedProcessUpdatedDate;
	}


//
//	public UUID getProcessId() {
//		return processId;
//	}
//
//	public void setProcessId(UUID processId) {
//		this.processId = processId;
//	}
	

}
