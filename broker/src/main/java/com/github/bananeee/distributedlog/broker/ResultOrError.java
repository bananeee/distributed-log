package com.github.bananeee.distributedlog.broker;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResultOrError<R, E> {
    private R result;
    private E error;

    public static <R, E> ResultOrError<R, E> success(R result) {
        return new ResultOrError<>(result, null);
    }

    public static <R, E> ResultOrError<R, E> error(E error) {
        return new ResultOrError<>(null, error);
    }

    public boolean isSuccess() {
        return error == null;
    }

    public boolean isError() {
        return error != null;
    }
}
