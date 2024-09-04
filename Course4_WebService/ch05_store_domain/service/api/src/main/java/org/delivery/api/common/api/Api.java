package org.delivery.api.common.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.delivery.api.common.error.ErrorCodeIfs;

import javax.validation.Valid;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Api<T> {
    private Result result;
    @Valid
    private T body;

    public static <T> Api<T> OK(T data) {
        Api<T> api = new Api<>();
        api.result = Result.OK();
        api.body = data;
        return api;
    }

    public static <T> Api<T> ERROR(Result result) {
        Api<T> api = new Api<>();
        api.result = Result.OK();
        return api;
    }

    public static <T> Api<T> ERROR(ErrorCodeIfs errorCodeIfs) {
        Api<T> api = new Api<>();
        api.result = Result.ERROR(errorCodeIfs);
        return api;
    }

    public static <T> Api<T> ERROR(ErrorCodeIfs errorCodeIfs, Throwable tx) {
        Api<T> api = new Api<>();
        api.result = Result.ERROR(errorCodeIfs, tx);
        return api;
    }

    public static <T> Api<T> ERROR(ErrorCodeIfs errorCodeIfs, String description) {
        Api<T> api = new Api<>();
        api.result = Result.ERROR(errorCodeIfs, description);
        return api;
    }

}
