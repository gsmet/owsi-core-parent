package fr.openwide.core.wicket.more.export.file.behavior;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.wicket.model.IModel;

import fr.openwide.core.commons.util.mime.MediaType;
import fr.openwide.core.wicket.more.export.AbstractDeferredDownloadBehavior;
import fr.openwide.core.wicket.more.model.BindingModel;
import fr.openwide.core.wicket.more.util.binding.CoreWicketMoreBindings;

public class FileDeferredDownloadBehavior extends AbstractDeferredDownloadBehavior {
	
	private static final long serialVersionUID = -2564879404137129896L;

	public static final String FILE_NAME_DATE_PATTERN_KEY = "common.action.export.file.datePattern";
	
	private final String fileDisplayNamePrefix;
	
	public FileDeferredDownloadBehavior(IModel<File> tempFileModel, IModel<MediaType> mediaTypeModel, String fileDisplayNamePrefix) {
		this(tempFileModel, mediaTypeModel, fileDisplayNamePrefix, true);
	}
	
	public FileDeferredDownloadBehavior(IModel<File> tempFileModel, IModel<MediaType> mediaTypeModel, String fileDisplayNamePrefix, boolean addAntiCache) {
		super(tempFileModel, BindingModel.of(mediaTypeModel, CoreWicketMoreBindings.mediaType().extension()), addAntiCache);
		this.fileDisplayNamePrefix = fileDisplayNamePrefix;
	}
	
	@Override
	protected String getFileDisplayName() {
		SimpleDateFormat dateFormat = new SimpleDateFormat(getComponent().getString(FILE_NAME_DATE_PATTERN_KEY));
		
		return new StringBuilder(fileDisplayNamePrefix)
				.append(dateFormat.format(new Date()))
				.append(".")
				.append(extensionModel.getObject())
				.toString();
	}
}