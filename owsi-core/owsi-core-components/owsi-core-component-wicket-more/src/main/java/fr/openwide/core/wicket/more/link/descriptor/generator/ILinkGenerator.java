package fr.openwide.core.wicket.more.link.descriptor.generator;

import org.apache.wicket.Component;
import org.apache.wicket.model.IComponentAssignedModel;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;

import fr.openwide.core.wicket.more.link.descriptor.AbstractDynamicBookmarkableLink;
import fr.openwide.core.wicket.more.link.descriptor.LinkInvalidTargetRuntimeException;
import fr.openwide.core.wicket.more.link.descriptor.parameter.injector.LinkParameterInjectionRuntimeException;
import fr.openwide.core.wicket.more.link.descriptor.parameter.validator.LinkParameterValidationRuntimeException;
import fr.openwide.core.wicket.more.link.util.LinkDescriptors;

/**
 * An utility object mapped to {@link IModel models}, that allows for simple link generation using these models to determine the target and parameters of the generated link.
 * 
 * <p>Re-implementing this interface is not recommended, as it may be extended with additional methods without prior notice.
 * <p><strong>Warning:</strong> this interface extends {@link IDetachable}. Thus, it <em>must</em> be detached before serialization.
 */
public interface ILinkGenerator extends IDetachable {

	/**
	 * Creates an {@link AbstractDynamicBookmarkableLink} that points to the same page/resource than this generator, with the same parameters.
	 * <p><strong>Note:</strong> special conditions apply to the rendering of this link if the parameters are invalid.
	 * See {@link AbstractDynamicBookmarkableLink} for more information.
	 * @return An {@link AbstractDynamicBookmarkableLink} matching this link generator.
	 * @see AbstractDynamicBookmarkableLink
	 */
	AbstractDynamicBookmarkableLink link(String wicketId);
	
	/**
	 * Renders the URL for this link generator.
	 * <p>The resulting string may be relative to the current request's URL, so it <strong>might not</string> include
	 * protocol ("http://"), host and port, but includes a path relative to the current request's path, as well as
	 * query parameters ("?arg0=true"), if any.
	 * @return The URL (relative if possible, absolute otherwise) for this link generator.
	 */
	String url() throws LinkInvalidTargetRuntimeException, LinkParameterInjectionRuntimeException,
			LinkParameterValidationRuntimeException;
	
	/**
	 * Renders the URL for this link generator.
	 * <p>The resulting string may be relative to the current request's URL, so it <strong>might not</string> include
	 * protocol ("http://"), host and port, but includes a path relative to the current request's path, as well as
	 * query parameters ("?arg0=true"), if any.
	 * 
	 * @return The URL (relative if possible, absolute otherwise) for this link generator.
	 * @param requestCycle The {@link RequestCycle} to use in order to generate the URL.
	 */
	String url(RequestCycle requestCycle) throws LinkInvalidTargetRuntimeException,
			LinkParameterInjectionRuntimeException, LinkParameterValidationRuntimeException;
	
	/**
	 * Renders the full (absolute) URL for this link generator.
	 * <p>The resulting string includes protocol ("http://"), host, port, and path, as well as query parameters ("?arg0=true"), if any.
	 * @return The full URL for this link generator.
	 */
	String fullUrl() throws LinkInvalidTargetRuntimeException, LinkParameterInjectionRuntimeException,
			LinkParameterValidationRuntimeException;
	
	/**
	 * Renders the full (absolute) URL for this link generator.
	 * <p>The resulting string includes protocol ("http://"), host, port, and path, as well as query parameters ("?arg0=true"), if any.
	 * @return The full URL for this link generator.
	 * @param requestCycle The {@link RequestCycle} to use in order to generate the URL.
	 */
	String fullUrl(RequestCycle requestCycle) throws LinkInvalidTargetRuntimeException,
			LinkParameterInjectionRuntimeException, LinkParameterValidationRuntimeException;

	/**
	 * Wraps any underlying {@link IComponentAssignedModel} and return the resulting link generator.
	 * 
	 * @see org.apache.wicket.model.IComponentAssignedModel.wrapOnAssignment(Component)
	 */
	ILinkGenerator wrap(Component component);
	
	/**
	 * Returns true if the generation methods ({@link #link(String)}, {@link #newRestartResponseException()}, etc.)
	 * are known not to throw any {@link LinkInvalidTargetRuntimeException} or
	 * {@link LinkParameterValidationRuntimeException}.
	 * <p><strong>WARNING:</strong> you should not use this method simply to detect validation errors! If there is no
	 * particular reason this link generator could be invalid, you should rely on exception handling instead.
	 * <p>The result of this method may change over time if the underlying data changes.
	 */
	boolean isAccessible();
	
	/**
	 * @deprecated Use {@link LinkDescriptors#invalid()} instead.
	 */
	@Deprecated
	ILinkGenerator INVALID = LinkDescriptors.invalid();

}
