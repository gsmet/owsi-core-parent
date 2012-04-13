package fr.openwide.core.jpa.more.util.image.service;

import java.io.File;

import fr.openwide.core.jpa.exception.SecurityServiceException;
import fr.openwide.core.jpa.exception.ServiceException;
import fr.openwide.core.jpa.more.util.image.model.ImageThumbnailFormat;

public interface IImageService {

	void generateThumbnail(File source, File destination, ImageThumbnailFormat thumbnailFormat)
			throws ServiceException, SecurityServiceException;

}