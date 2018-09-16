package com.training.dao;

import java.util.List;
import java.util.UUID;

import javax.annotation.ManagedBean;
import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.training.datastax.TrainingDatastaxEntityManager;
import com.training.datastax.TrainingDatastaxQuery;
import com.training.datastax.QueryCache;
import com.training.entity.FilesByUserEntity;
import com.training.exception.DBRemoteException;

@ManagedBean
public class ProjectFileDaoBean {

	private static final Logger LOGGER = LogManager.getLogger(ProjectFileDaoBean.class.getName());

	@Inject
	@com.training.datastax.TrainingDatastaxPersistenceContext(clusterId = "Training")
	TrainingDatastaxEntityManager entityManager;

	@Inject
	QueryCache queryCache;

	public List<FilesByUserEntity> getFilesByUserId(UUID userId) {
		LOGGER.info("getFilesByUserId(UUID userId) called " + userId);
		TrainingDatastaxQuery<FilesByUserEntity> query = new TrainingDatastaxQuery<FilesByUserEntity>(
				entityManager.getSession(), queryCache.getFilesByUserId(), FilesByUserEntity.class);
		query.setParameter(0, userId);
		return query.getResultList();
	}


	public List<FilesByUserEntity> getFilesByFileId(UUID fileId) {
		LOGGER.info("getFilesByFileId(UUID fileId) called " + fileId);
		TrainingDatastaxQuery<FilesByUserEntity> query = new TrainingDatastaxQuery<FilesByUserEntity>(
				entityManager.getSession(), queryCache.getFilesByFileId(), FilesByUserEntity.class);
		query.setParameter(0, fileId);
		return query.getResultList();
	}
	

	public boolean saveFile(FilesByUserEntity projectFileEntity) {
		boolean result = false;
		try {
			entityManager.persist(projectFileEntity);
			result = true;
			LOGGER.info("save file operation " + result);
		} catch (Exception e) {
			LOGGER.error("saving file failed" + e.getMessage(),e);
			throw new DBRemoteException("save process failed" + e.getMessage(), e);
		}

		return result;

	}

	public boolean updateFile(FilesByUserEntity projectFileEntity) {
		boolean result = false;
		try {
			entityManager.merge(projectFileEntity);
			result = true;
		} catch (Exception ex) {
			LOGGER.error("Error updateFile", ex);
			result = false;
			throw new RuntimeException(ex.getMessage(),ex);
		}
		return result;
	}
	
	
	 

}
