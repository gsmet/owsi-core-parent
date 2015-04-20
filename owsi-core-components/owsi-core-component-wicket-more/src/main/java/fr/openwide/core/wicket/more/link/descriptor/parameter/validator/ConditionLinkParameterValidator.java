package fr.openwide.core.wicket.more.link.descriptor.parameter.validator;

import java.util.Collection;

import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.lang.Args;
import org.bindgen.BindingRoot;
import org.javatuples.Unit;

import com.google.common.base.Predicate;

import fr.openwide.core.wicket.more.condition.Condition;
import fr.openwide.core.wicket.more.link.descriptor.parameter.validator.factory.AbstractLinkParameterValidatorFactory;
import fr.openwide.core.wicket.more.link.descriptor.parameter.validator.factory.ILinkParameterValidatorFactory;
import fr.openwide.core.wicket.more.model.BindingModel;

public class ConditionLinkParameterValidator implements ILinkParameterValidator {
	
	private static final long serialVersionUID = -6678335084190190566L;

	private final Condition condition;
	
	public static <R> ILinkParameterValidatorFactory<Unit<IModel<? extends R>>> predicateFactory(final Predicate<? super R> predicate) {
		Args.notNull(predicate, "predicate");
		return new AbstractLinkParameterValidatorFactory<Unit<IModel<? extends R>>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public ILinkParameterValidator create(Unit<IModel<? extends R>> parameters) {
				return new ConditionLinkParameterValidator(Condition.predicate(parameters.getValue0(), predicate));
			}
		};
	}
	
	public static <R> ILinkParameterValidatorFactory<Unit<IModel<? extends R>>> anyPermissionFactory(final Collection<String> permissions) {
		Args.notNull(permissions, "permissions");
		return new AbstractLinkParameterValidatorFactory<Unit<IModel<? extends R>>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public ILinkParameterValidator create(Unit<IModel<? extends R>> parameters) {
				return new ConditionLinkParameterValidator(Condition.anyPermission(parameters.getValue0(), permissions));
			}
		};
	}
	
	public static <R, T> ILinkParameterValidatorFactory<Unit<IModel<? extends R>>> anyPermissionFactory(
			final BindingRoot<R, T> bindingRoot, final Collection<String> permissions) {
		Args.notNull(bindingRoot, "bindingRoot");
		Args.notNull(permissions, "permissions");
		return new AbstractLinkParameterValidatorFactory<Unit<IModel<? extends R>>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public ILinkParameterValidator create(Unit<IModel<? extends R>> parameters) {
				return new ConditionLinkParameterValidator(Condition.anyPermission(BindingModel.of(parameters.getValue0(), bindingRoot), permissions));
			}
		};
	}
	
	public ConditionLinkParameterValidator(Condition condition) {
		this.condition = condition;
	}

	@Override
	public void validateSerialized(PageParameters parameters, LinkParameterValidationErrorCollector collector) {
		// Nothing to do
	}

	@Override
	public void validateModel(LinkParameterValidationErrorCollector collector) {
		if (!condition.applies()) {
			collector.addError(String.format("Condition '%s' was false.", condition));
		}
	}

	@Override
	public void detach() {
		condition.detach();
	}

}
