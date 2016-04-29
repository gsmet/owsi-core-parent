package fr.openwide.core.wicket.more.condition;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.acls.domain.PermissionFactory;
import org.springframework.security.acls.model.Permission;

import com.google.common.base.Equivalence;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import fr.openwide.core.commons.util.functional.Predicates2;
import fr.openwide.core.jpa.security.service.IAuthenticationService;
import fr.openwide.core.wicket.more.markup.html.basic.ComponentBooleanProperty;
import fr.openwide.core.wicket.more.markup.html.basic.ComponentBooleanPropertyBehavior;
import fr.openwide.core.wicket.more.markup.html.basic.impl.AbstractConfigurableComponentBooleanPropertyBehavior.Operator;
import fr.openwide.core.wicket.more.markup.repeater.sequence.ISequenceProvider;
import fr.openwide.core.wicket.more.model.BindingModel;
import fr.openwide.core.wicket.more.util.Detach;
import fr.openwide.core.wicket.more.util.binding.CoreWicketMoreBindings;

public abstract class Condition implements IModel<Boolean>, IDetachable {
	
	private static final long serialVersionUID = -3315852580233582804L;
	
	private static final Joiner COMMA_JOINER = Joiner.on(',');
	
	public abstract boolean applies();
	
	/**
	 * @deprecated Provided only to satisfy the {@link IModel} interface. Use {@link #applies()} instead.
	 */
	@Override
	@Deprecated
	public final Boolean getObject() {
		return applies();
	}

	/**
	 * @deprecated Provided only to satisfy the {@link IModel} interface. Not supported.
	 */
	@Override
	@Deprecated
	public void setObject(Boolean object) {
		throw new UnsupportedOperationException("Cannot set the value of a condition");
	}

	@Override
	public void detach() { }
	
	public Condition or(Condition operand) {
		return composite(BooleanOperator.OR, this, operand);
	}
	
	public Condition nor(Condition operand) {
		return composite(BooleanOperator.NOR, this, operand);
	}
	
	public Condition and(Condition operand) {
		return composite(BooleanOperator.AND, this, operand);
	}
	
	public Condition nand(Condition operand) {
		return composite(BooleanOperator.NAND, this, operand);
	}
	
	public Condition negate() {
		return new NegateCondition(this);
	}
	
	private static class NegateCondition extends Condition {
		private static final long serialVersionUID = 1L;

		private final Condition condition;
		
		public NegateCondition(Condition condition) {
			super();
			this.condition = condition;
		}
		
		@Override
		public boolean applies() {
			return !condition.applies();
		}
		
		@Override
		public void detach() {
			super.detach();
			condition.detach();
		}
		
		@Override
		public String toString() {
			return "not(" + condition + ")";
		}
	}
	
	public <T> IfElseBuilder<T> then(IModel<? extends T> modelIfTrue) {
		return new IfElseBuilder<>(this, modelIfTrue);
	}
	
	public <T extends Serializable> ValueIfElseBuilder<T> then(T valueIfTrue) {
		return new ValueIfElseBuilder<>(this, Model.of(valueIfTrue));
	}
	
	public static class IfElseBuilder<T> {
		private final Condition condition;
		private final IModel<? extends T> modelIfTrue;
		public IfElseBuilder(Condition condition, IModel<? extends T> modelIfTrue) {
			super();
			this.condition = condition;
			this.modelIfTrue = modelIfTrue;
		}
		public IfElseBuilder<T> elseIf(Condition condition, IModel<? extends T> model) {
			return new IfElseBuilder<T>(condition, model) {
				@Override
				public IModel<T> otherwise(IModel<? extends T> modelIfFalse) {
					return IfElseBuilder.this.otherwise(super.otherwise(modelIfFalse));
				}
			};
		}
		public IModel<T> otherwise(IModel<? extends T> modelIfFalse) {
			return condition.asValue(modelIfTrue, modelIfFalse);
		}
	}
	
	public static class ValueIfElseBuilder<T extends Serializable> extends IfElseBuilder<T> {
		public ValueIfElseBuilder(Condition condition, IModel<? extends T> modelIfTrue) {
			super(condition, modelIfTrue);
		}
		@Override
		public ValueIfElseBuilder<T> elseIf(Condition condition, IModel<? extends T> model) {
			return new ValueIfElseBuilder<T>(condition, model) {
				@Override
				public IModel<T> otherwise(IModel<? extends T> modelIfFalse) {
					return ValueIfElseBuilder.this.otherwise(super.otherwise(modelIfFalse));
				}
			};
		}
		public ValueIfElseBuilder<T> elseIf(Condition condition, T valueIfFalse) {
			return elseIf(condition, Model.of(valueIfFalse));
		}
		public IModel<T> otherwise(T valueIfFalse) {
			return otherwise(Model.of(valueIfFalse));
		}
	}
	
	/**
	 * @deprecated Use .then(...).otherwise(...) instead.
	 */
	@Deprecated
	public <T> IModel<T> asValue(IModel<? extends T> modelIfTrue, IModel<? extends T> modelIfFalse) {
		return new ConditionalModel<>(this, modelIfTrue, modelIfFalse);
	}

	/**
	 * @deprecated Use .then(...).otherwise(...) instead.
	 */
	@Deprecated
	public <T extends Serializable> IModel<T> asValue(T valueIfTrue, T valueIfFalse) {
		return new ConditionalModel<>(this, Model.of(valueIfTrue), Model.of(valueIfFalse));
	}
	
	private static final class ConditionalModel<T> extends AbstractReadOnlyModel<T> {
		private static final long serialVersionUID = 4696234484508240728L;
		
		private final Condition condition;
		
		private IModel<? extends T> modelIfTrue;
		
		private IModel<? extends T> modelIfFalse;
		
		private ConditionalModel(Condition condition, IModel<? extends T> modelIfTrue, IModel<? extends T> modelIfFalse) {
			super();
			this.condition = checkNotNull(condition);
			this.modelIfTrue = checkNotNull(modelIfTrue);
			this.modelIfFalse = checkNotNull(modelIfFalse);
		}
		
		@Override
		public T getObject() {
			if (condition.applies()) {
				return modelIfTrue.getObject();
			} else {
				return modelIfFalse.getObject();
			}
		}
		
		@Override
		public void detach() {
			super.detach();
			condition.detach();
			modelIfTrue.detach();
			modelIfFalse.detach();
		}
	}
	
	public static Condition composite(BooleanOperator operator, Condition ... operands) {
		return new CompositeCondition(operator, Arrays.asList(operands));
	}
	
	public static Condition composite(BooleanOperator operator, Iterable<? extends Condition> operands) {
		return new CompositeCondition(operator, operands);
	}
	
	private static class CompositeCondition extends Condition {
		private static final long serialVersionUID = 1L;
		
		private final BooleanOperator operator;

		private final Iterable<? extends Condition> operands;
		
		public CompositeCondition(BooleanOperator operator, Iterable<? extends Condition> operands) {
			super();
			this.operator = operator;
			this.operands = ImmutableList.copyOf(operands);
		}

		@Override
		public boolean applies() {
			return operator.apply(operands);
		}
		
		@Override
		public void detach() {
			super.detach();
			for (Condition operand : operands) {
				operand.detach();
			}
		}
		
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append(operator.name().toLowerCase()).append("(").append(COMMA_JOINER.join(operands)).append(")");
			return builder.toString();
		}
	}
	
	public static Condition constant(boolean value) {
		return value ? alwaysTrue() : alwaysFalse();
	}
	
	public static Condition alwaysTrue() {
		return ConstantCondition.ALWAYS_TRUE;
	}
	
	public static Condition alwaysFalse() {
		return ConstantCondition.ALWAYS_FALSE;
	}
	
	private static class ConstantCondition extends Condition {
		private static final long serialVersionUID = -7678144550356610455L;
		
		private static ConstantCondition ALWAYS_TRUE = new ConstantCondition(true) {
			private static final long serialVersionUID = -8786829515620843503L;
			private Object readResolve() {
				return ALWAYS_TRUE;
			}
		};
		private static ConstantCondition ALWAYS_FALSE = new ConstantCondition(false) {
			private static final long serialVersionUID = -6055735778127387150L;
			private Object readResolve() {
				return ALWAYS_FALSE;
			}
		};
		
		private final boolean value;

		public ConstantCondition(boolean value) {
			super();
			this.value = value;
		}
		
		@Override
		public boolean applies() {
			return value;
		}
		
		@Override
		public String toString() {
			return String.valueOf(value);
		}
	}
	
	public static <T> Condition isEqual(IModel<? extends T> leftModel, IModel<? extends T> rightModel) {
		return isEquivalent(leftModel, rightModel, Equivalence.equals());
	}
	
	public static <T> Condition isEquivalent(IModel<? extends T> leftModel, IModel<? extends T> rightModel, Equivalence<? super T> equivalence) {
		return new EquivalenceCondition<>(leftModel, rightModel, equivalence);
	}
	
	private static class EquivalenceCondition<T> extends Condition {
		private static final long serialVersionUID = 1L;

		private final IModel<? extends T> leftModel;
		private final IModel<? extends T> rightModel;
		private final Equivalence<? super T> equivalence;
		
		public EquivalenceCondition(IModel<? extends T> leftModel, IModel<? extends T> rightModel, Equivalence<? super T> equivalence) {
			super();
			this.leftModel = leftModel;
			this.rightModel = rightModel;
			this.equivalence = equivalence;
		}
		
		@Override
		public boolean applies() {
			return equivalence.equivalent(leftModel.getObject(), rightModel.getObject());
		}
		
		@Override
		public void detach() {
			super.detach();
			if (equivalence instanceof IDetachable) {
				((IDetachable)equivalence).detach();
			}
			leftModel.detach();
			rightModel.detach();
		}
		
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append(equivalence).append("(").append(COMMA_JOINER.join(leftModel, rightModel)).append(")");
			return builder.toString();
		}
	}
	
	public static <T> Condition contains(IModel<? extends Collection<? super T>> collectionModel, IModel<? extends T> elementModel) {
		return new ContainsCondition<>(collectionModel, elementModel);
	}
	
	private static class ContainsCondition<T> extends Condition {
		private static final long serialVersionUID = 1L;

		private final IModel<? extends Collection<? super T>> collectionModel;
		private final IModel<? extends T> elementModel;
		
		public ContainsCondition(IModel<? extends Collection<? super T>> collectionModel, IModel<? extends T> elementModel) {
			super();
			this.collectionModel = collectionModel;
			this.elementModel = elementModel;
		}
		
		@Override
		public boolean applies() {
			Collection<? super T> collection = collectionModel.getObject();
			T element = elementModel.getObject();
			return collection != null && collection.contains(element);
		}
		
		@Override
		public void detach() {
			super.detach();
			collectionModel.detach();
			elementModel.detach();
		}
		
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("contains(").append(COMMA_JOINER.join(collectionModel, elementModel)).append(")");
			return builder.toString();
		}
	}
	
	public static <T> Condition predicate(IModel<? extends T> model, Predicate<? super T> predicate) {
		return predicate(model, Detach.YES, predicate);
	}
	
	public static <T1, T2> Condition predicate(IModel<? extends T2> model, Function<? super T2, ? extends T1> function, Predicate<? super T1> predicate) {
		return predicate(model, Detach.YES, function, predicate);
	}
	
	public static <T> Condition predicate(IModel<? extends T> model, Detach detachModel, Predicate<? super T> predicate) {
		return new PredicateCondition<>(model, detachModel, predicate);
	}
	
	public static <T> Condition convertedInputPredicate(final FormComponent<? extends T> formComponent, Predicate<? super T> predicate) {
		return convertedInputPredicate(formComponent, Detach.YES, predicate);
	}
	
	public static <T1, T2> Condition predicate(IModel<? extends T2> model, Detach detachModel, Function<? super T2, ? extends T1> function, Predicate<? super T1> predicate) {
		return predicate(model, detachModel, Predicates.compose(predicate, function));
	}
	
	public static <T> Condition convertedInputPredicate(final FormComponent<? extends T> formComponent, Detach detachModel, Predicate<? super T> predicate) {
		return predicate(
				new AbstractReadOnlyModel<T>() {
					private static final long serialVersionUID = 1L;
					@Override
					public T getObject() {
						return formComponent.getConvertedInput();
					}
					@Override
					public String toString() {
						return formComponent.toString();
					}
				},
				detachModel,
				predicate
		);
	}
	
	private static class PredicateCondition<T> extends Condition {
		private static final long serialVersionUID = 1L;

		private final IModel<? extends T> model;
		private final Detach detachModel;
		private final Predicate<? super T> predicate;
		
		public PredicateCondition(IModel<? extends T> model, Detach detachModel, Predicate<? super T> predicate) {
			super();
			this.model = model;
			this.detachModel = detachModel;
			this.predicate = predicate;
		}
		
		@Override
		public boolean applies() {
			return predicate.apply(model.getObject());
		}
		
		@Override
		public void detach() {
			super.detach();
			if (predicate instanceof IDetachable) {
				((IDetachable)predicate).detach();
			}
			if (Detach.YES.equals(detachModel)) {
				model.detach();
			}
		}
		
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append(predicate).append("(").append(model).append(")");
			return builder.toString();
		}
	}
	
	/**
	 * @see Predicates2#isTrue()
	 */
	public static Condition isTrue(IModel<Boolean> model) {
		return predicate(model, Predicates2.isTrue());
	}

	/**
	 * @see Predicates2#isTrueOrNull()
	 */
	public static Condition isTrueOrNull(IModel<Boolean> model) {
		return predicate(model, Predicates2.isTrueOrNull());
	}

	/**
	 * @see Predicates2#isFalse()
	 */
	public static Condition isFalse(IModel<Boolean> model) {
		return predicate(model, Predicates2.isFalse());
	}

	/**
	 * @see Predicates2#isFalseOrNull()
	 */
	public static Condition isFalseOrNull(IModel<Boolean> model) {
		return predicate(model, Predicates2.isFalseOrNull());
	}
	
	public static Condition isEmpty(IDataProvider<?> dataProvider) {
		return predicate(BindingModel.of(dataProvider, CoreWicketMoreBindings.iBindableDataProvider().size()), Predicates.equalTo(0L));
	}
	
	public static Condition isNotEmpty(IDataProvider<?> dataProvider) {
		return predicate(BindingModel.of(dataProvider, CoreWicketMoreBindings.iBindableDataProvider().size()), Predicates.equalTo(0L)).negate();
	}
	
	public static Condition isEmpty(final ISequenceProvider<?> sequenceProvider) {
		return new Condition() {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean applies() {
				return sequenceProvider.size() == 0;
			}
			@Override
			public void detach() {
				sequenceProvider.detach();
			}
		};
	}
	
	public static Condition isNotEmpty(final ISequenceProvider<?> sequenceProvider) {
		return new Condition() {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean applies() {
				return sequenceProvider.size() > 0;
			}
			@Override
			public void detach() {
				sequenceProvider.detach();
			}
		};
	}
	
	public static Condition visible(Component component) {
		return new VisibleCondition(component);
	}
	
	private static class VisibleCondition extends Condition {
		private static final long serialVersionUID = 1L;
		
		private final Component component;
		
		public VisibleCondition(Component component) {
			super();
			this.component = component;
		}
		
		@Override
		public boolean applies() {
			component.configure();
			return component.determineVisibility();
		}
		
		@Override
		public String toString() {
			return "visible(" + component + ")";
		}
	}
	
	public static Condition anyChildVisible(MarkupContainer container) {
		return new AnyChildVisibleCondition(container);
	}
	
	private static class AnyChildVisibleCondition extends Condition {
		private static final long serialVersionUID = 1L;
		
		private final MarkupContainer container;
		
		public AnyChildVisibleCondition(MarkupContainer container) {
			super();
			this.container = container;
		}
		
		@Override
		public boolean applies() {
			for (Component child : container) {
				child.configure();
				if (child.determineVisibility()) {
					return true;
				}
			}
			return false;
		}
		
		@Override
		public String toString() {
			return "anyChildVisible(" + container + ")";
		}
	}
	
	public static Condition anyChildEnabled(MarkupContainer container) {
		return new AnyChildEnabledCondition(container);
	}
	
	private static class AnyChildEnabledCondition extends Condition {
		private static final long serialVersionUID = 1L;
		
		private final MarkupContainer container;
		
		public AnyChildEnabledCondition(MarkupContainer container) {
			super();
			this.container = container;
		}
		
		@Override
		public boolean applies() {
			for (Component child : container) {
				child.configure();
				if (child.isEnabled() && child.isEnableAllowed()) {
					return true;
				}
			}
			return false;
		}
		
		@Override
		public String toString() {
			return "anyChildEnabled(" + container + ")";
		}
	}
	
	public static Condition role(String role) {
		return AnyRoleCondition.fromStrings(ImmutableList.of(role));
	}
	
	public static Condition anyRole(String role, String ... otherRoles) {
		return AnyRoleCondition.fromStrings(Lists.asList(role, otherRoles));
	}
	
	public static Condition role(IModel<String> roleModel) {
		return AnyRoleCondition.fromModels(ImmutableList.of(roleModel));
	}
	
	@SafeVarargs
	public static Condition anyRole(IModel<String> roleModel, IModel<String> ... otherRoleModels) {
		return AnyRoleCondition.fromModels(Lists.asList(roleModel, otherRoleModels));
	}
	
	private static class AnyRoleCondition extends Condition {
		private static final long serialVersionUID = 1L;
		
		@SpringBean
		private IAuthenticationService authenticationService;
		
		private final Iterable<? extends IModel<String>> roleModels;
		
		public static AnyRoleCondition fromStrings(Iterable<String> roles) {
			return new AnyRoleCondition(Iterables.transform(roles, new Function<String, IModel<String>>() {
				@Override
				public IModel<String> apply(String input) {
					return Model.of(input);
				}
			}));
		}
		
		public static AnyRoleCondition fromModels(Iterable<? extends IModel<String>> roleModels) {
			return new AnyRoleCondition(roleModels);
		}
		
		private AnyRoleCondition(Iterable<? extends IModel<String>> roleModels) {
			super();
			Injector.get().inject(this);
			this.roleModels = ImmutableSet.copyOf(roleModels);
		}
		
		@Override
		public boolean applies() {
			for (IModel<String> roleModel : roleModels) {
				if (authenticationService.hasRole(roleModel.getObject())) {
					return true;
				}
			}
			return false;
		}
		
		@Override
		public void detach() {
			super.detach();
			for (IModel<String> roleModel : roleModels) {
				roleModel.detach();
			}
		}
		
		@Override
		public String toString() {
			return "anyRole(" + COMMA_JOINER.join(roleModels) + ")";
		}
	}
	
	public static Condition permission(String permissionName) {
		return new AnyGlobalPermissionCondition(ImmutableList.of(permissionName));
	}
	
	public static Condition anyPermission(String permissionName, String ... otherPermissionNames) {
		return new AnyGlobalPermissionCondition(Lists.asList(permissionName, otherPermissionNames));
	}
	
	public static Condition anyPermission(Iterable<String> permissionNames) {
		return new AnyGlobalPermissionCondition(permissionNames);
	}
	
	public static Condition permission(Permission permission) {
		return new AnyGlobalPermissionCondition(permission);
	}
	
	private static class AnyGlobalPermissionCondition extends Condition {
		private static final long serialVersionUID = 1L;
		
		@SpringBean
		private PermissionFactory permissionFactory;
		
		@SpringBean
		private IAuthenticationService authenticationService;
		
		private final Iterable<Permission> permissions;
		
		public AnyGlobalPermissionCondition(Iterable<String> permissionNames) {
			super();
			Injector.get().inject(this);
			this.permissions = permissionFactory.buildFromNames(ImmutableList.copyOf(permissionNames));
		}
		
		public AnyGlobalPermissionCondition(Permission permission) {
			super();
			Injector.get().inject(this);
			this.permissions = ImmutableList.of(permission);
		}
		
		@Override
		public boolean applies() {
			for (Permission permission : permissions) {
				if (authenticationService.hasPermission(permission)) {
					return true;
				}
			}
			return false;
		}
		
		@Override
		public String toString() {
			return "anyGlobalPermission(" + COMMA_JOINER.join(permissions) + ")";
		}
	}
	
	public static Condition permission(IModel<?> securedObjectModel, String permissionName) {
		return new AnyObjectPermissionCondition(securedObjectModel, ImmutableList.of(permissionName));
	}
	
	public static Condition anyPermission(IModel<?> securedObjectModel, String permissionName, String ... otherPermissionNames) {
		return new AnyObjectPermissionCondition(securedObjectModel, Lists.asList(permissionName, otherPermissionNames));
	}
	
	public static Condition anyPermission(IModel<?> securedObjectModel, Iterable<String> permissionNames) {
		return new AnyObjectPermissionCondition(securedObjectModel, permissionNames);
	}
	
	public static Condition permission(IModel<?> securedObjectModel, Permission permission) {
		return new AnyObjectPermissionCondition(securedObjectModel, permission);
	}
	
	private static class AnyObjectPermissionCondition extends Condition {
		private static final long serialVersionUID = 1L;
		
		@SpringBean
		private PermissionFactory permissionFactory;
		
		@SpringBean
		private IAuthenticationService authenticationService;
		
		private final IModel<?> securedObjectModel;
		
		private final Iterable<Permission> permissions;
		
		public AnyObjectPermissionCondition(IModel<?> securedObjectModel, Iterable<String> permissionNames) {
			super();
			Injector.get().inject(this);
			this.securedObjectModel = securedObjectModel;
			this.permissions = permissionFactory.buildFromNames(ImmutableList.copyOf(permissionNames));
		}
		
		public AnyObjectPermissionCondition(IModel<?> securedObjectModel, Permission permission) {
			super();
			Injector.get().inject(this);
			this.securedObjectModel = securedObjectModel;
			this.permissions = ImmutableList.of(permission);
		}
		
		@Override
		public boolean applies() {
			Object securedObject = securedObjectModel.getObject();
			for (Permission permission : permissions) {
				if (authenticationService.hasPermission(securedObject, permission)) {
					return true;
				}
			}
			return false;
		}
		
		@Override
		public void detach() {
			super.detach();
			securedObjectModel.detach();
		}
		
		@Override
		public String toString() {
			return "anyObjectPermission(" + securedObjectModel + "," + COMMA_JOINER.join(permissions) + ")";
		}
	}
	
	public static Condition modelNotNull(IModel<?> model) {
		return predicate(model, Predicates.notNull());
	}
	
	public static <C extends Collection<?>> Condition collectionModelNotEmpty(IModel<C> collectionModel) {
		return predicate(collectionModel, Predicates2.notEmpty());
	}
	
	public static Condition modelsAnyNotNull(IModel<?> firstModel, IModel<?>... otherModels) {
		Condition condition = Condition.alwaysFalse();
		
		for (IModel<?> model : Lists.asList(firstModel, otherModels)) {
			condition.or(modelNotNull(model));
		}
		return condition;
	}

	@SafeVarargs
	public final <T> Condition predicateAnyTrue(Predicate<? super T> predicate, IModel<? extends T> firstModel,
			IModel<? extends T>... otherModels) {
		Condition condition = Condition.alwaysFalse();
		
		for (IModel<? extends T> model : Lists.asList(firstModel, otherModels)) {
			condition.or(predicate(model, predicate));
		}
		
		return condition;
	}
	
	public static Condition componentVisible(Component component) {
		return visible(component);
	}
	
	public static Condition componentsAnyVisible(Component firstComponent, Component... otherComponents) {
		return componentsAnyVisible(Lists.asList(firstComponent, otherComponents));
	}
	
	public static Condition componentsAnyVisible(Collection<? extends Component> targetComponents) {
		Condition condition = Condition.alwaysFalse();
		
		for (Component component : targetComponents) {
			condition.or(visible(component));
		}
		
		return condition;
	}
	
	public Behavior thenShow() {
		return thenProperty(ComponentBooleanProperty.VISIBILITY_ALLOWED);
	}
	
	public Behavior thenHide() {
		return thenPropertyNegate(ComponentBooleanProperty.VISIBILITY_ALLOWED);
	}
	
	public Behavior thenShowInternal() {
		return thenProperty(ComponentBooleanProperty.VISIBLE);
	}
	
	public Behavior thenHideInternal() {
		return thenPropertyNegate(ComponentBooleanProperty.VISIBLE);
	}
	
	public Behavior thenEnable() {
		return thenProperty(ComponentBooleanProperty.ENABLE);
	}
	
	public Behavior thenProperty(ComponentBooleanProperty property) {
		return thenPropertyNegate(ComponentBooleanProperty.ENABLE);
	}
	
	public Behavior thenPropertyNegate(ComponentBooleanProperty property) {
		return new ComponentBooleanPropertyBehavior(ComponentBooleanProperty.ENABLE, Operator.WHEN_ALL_TRUE)
				.condition(this.negate());
	}
}
