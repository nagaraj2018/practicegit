package com.training.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import com.training.exception.AppException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Provider
public class AppExceptionMapper implements ExceptionMapper<AppException> {
    private static final Logger LOGGER = LogManager.getLogger(AppExceptionMapper.class.getName());

    public Response toResponse(AppException ex) {
	LOGGER.info("toResponse(AppException ex) called");
	return Response.status(ex.getStatus()).entity(new ErrorMessage(ex)).type(MediaType.APPLICATION_JSON).build();
    }

}
