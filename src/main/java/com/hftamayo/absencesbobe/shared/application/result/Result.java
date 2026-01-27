package com.hftamayo.absencesbobe.shared.application.result;

import java.util.Objects;
import java.util.Optional;

public record Result<T, E>(T value, E error) {

    public Result {
        boolean hasValue = value != null;
        boolean hasError = error != null;

        if (hasValue == hasError) { // both true or both false
            throw new IllegalArgumentException("Result must have exactly one of value or error");
        }
    }

    public static <T, E> Result<T, E> ok(T value) {
        return new Result<>(Objects.requireNonNull(value, "value must not be null"), null);
    }

    public static <T, E> Result<T, E> error(E error) {
        return new Result<>(null, Objects.requireNonNull(error, "error must not be null"));
    }

    public boolean isSuccess() {
        return value != null;
    }

    public boolean isError() {
        return error != null;
    }

    public T getValueOrThrow() {
        if (value == null) throw new IllegalStateException("Result is error");
        return value;
    }

    public E getErrorOrThrow() {
        if (error == null) throw new IllegalStateException("Result is success");
        return error;
    }

    public Optional<T> valueOpt() {
        return Optional.ofNullable(value);
    }

    public Optional<E> errorOpt() {
        return Optional.ofNullable(error);
    }
}