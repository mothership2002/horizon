# Annotation Improvements

## Overview

This document describes the improvements made to the `@Intent` and `@Conductor` annotations in the Horizon framework to make them more protocol-agnostic.

## Intent Annotation

The `@Intent` annotation has been enhanced to be more protocol-agnostic, focusing on the intent/purpose rather than HTTP-specific details.

### Before

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Intent {
    String value();
    String path();
}
```

### After

```java
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Intent {
    String value() default "";
    String name() default "";
    String[] metadata() default {};
}
```

### Improvements

1. **Extended Target**: The annotation can now be applied to both classes and methods, allowing for more flexible mapping.
2. **Default Values**: All attributes now have default values, making the annotation more convenient to use.
3. **Protocol-Agnostic**: Removed HTTP-specific attributes like `path`, `method`, `params`, `headers`, `consumes`, and `produces`.
4. **Intent Name**: Added a `name` attribute as an alias for `value` to specify the intent name.
5. **Metadata**: Added a `metadata` attribute to provide additional information about the intent.
6. **Documentation**: Added comprehensive JavaDoc documentation for the annotation and its attributes.

## Conductor Annotation

The `@Conductor` annotation has been enhanced to be more protocol-agnostic, focusing on the intent/purpose rather than HTTP-specific details.

### Before

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Conductor {
}
```

### After

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Conductor {
    String value() default "";
    String namespace() default "";
    boolean transactional() default false;
    boolean validated() default true;
}
```

### Improvements

1. **Name**: Added a `value` attribute to specify the name of the conductor.
2. **Namespace**: Added a `namespace` attribute to specify a namespace for all intents handled by the conductor.
3. **Transactional Support**: Added a `transactional` attribute to indicate whether the conductor's methods should be executed within a transaction.
4. **Validation Support**: Added a `validated` attribute to indicate whether method parameters should be validated.
5. **Documentation**: Added comprehensive JavaDoc documentation for the annotation and its attributes.

## ActionType Enum

Created a new `ActionType` enum to provide a protocol-agnostic way to specify the type of action that an intent represents.

```java
public enum ActionType {
    READ,
    CREATE,
    UPDATE,
    DELETE,
    QUERY,
    EXECUTE,
    SUBSCRIBE,
    UNSUBSCRIBE
}
```

## Sample Usage

A sample `UserConductor` class has been created to demonstrate how to use the enhanced annotations:

```java
@Conductor(namespace = "users")
public class UserConductor implements horizon.core.conductor.Conductor<Map<String, Object>> {

    @Intent(value = "getAllUsers", metadata = {"action=read", "target=users"})
    public Command<Map<String, Object>> getAllUsers() {
        // ...
    }

    @Intent(value = "getUserById", metadata = {"action=read", "target=user", "param=id"})
    public Command<Map<String, Object>> getUserById(Map<String, Object> payload) {
        // ...
    }

    @Intent(value = "createUser", metadata = {"action=create", "target=user"})
    public Command<Map<String, Object>> createUser(Map<String, Object> payload) {
        // ...
    }

    @Intent(value = "updateUser", metadata = {"action=update", "target=user", "param=id"})
    public Command<Map<String, Object>> updateUser(Map<String, Object> payload) {
        // ...
    }

    @Intent(value = "deleteUser", metadata = {"action=delete", "target=user", "param=id"})
    public Command<Map<String, Object>> deleteUser(Map<String, Object> payload) {
        // ...
    }

    @Override
    public Command<?> resolve(Map<String, Object> payload) {
        // ...
    }
}
```

## Conclusion

These improvements make the annotations more protocol-agnostic, focusing on the intent/purpose rather than protocol-specific details. This allows the framework to be used with any protocol, not just HTTP. The `@Intent` annotation now provides a way to map requests to methods based on the intent name, and the `@Conductor` annotation now provides a way to group related intents under a common namespace.
