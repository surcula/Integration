package be.atc.erpprojetintegration_1.tools;

import java.util.Map;

/**
 * A generic wrapper class used to represent the outcome of an operation.
 * It contains either a successful result with associated data or a failure with error messages.
 *
 * @param <T> The type of data returned by the operation.
 */
public class Result<T> {
    private boolean success;
    private T data;
    private Map<String, String> errors;

    /**
     * Private constructor to initialize a Result instance.
     *
     * @param success Indicates if the operation was successful.
     * @param errors  A map of error messages if the operation failed.
     * @param data    The data returned if the operation succeeded.
     */
    private Result(boolean success, Map<String, String> errors, T data) {
        this.success = success;
        this.errors = errors;
        this.data = data;
    }

    /**
     * Creates a successful Result with the provided data.
     *
     * @param data The data to return.
     * @param <T>  The type of the data.
     * @return A Result object marked as successful containing the data.
     */
    public static <T> Result<T> ok(T data) {
        return new Result<>(true, null, data);
    }

    /**
     * Creates a successful Result without any data (typically for operations returning void).
     *
     * @return A Result object marked as successful with no data.
     */
    public static Result<Void> ok() {
        return new Result<>(true, null, null);
    }

    /**
     * Creates a failed Result with the provided error messages.
     *
     * @param errors A map of error messages, typically with field names as keys.
     * @param <T>    The type of data expected if the operation had succeeded.
     * @return A Result object marked as failed containing the errors.
     */
    public static <T> Result<T> fail(Map<String, String> errors) {
        return new Result<>(false, errors, null);
    }

    /**
     * Indicates whether the operation was successful.
     *
     * @return true if the operation succeeded, false otherwise.
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns the data associated with a successful operation.
     *
     * @return The data if present, or null if the operation failed or returned no data.
     */
    public T getData() {
        return data;
    }

    /**
     * Returns the error messages associated with a failed operation.
     *
     * @return A map of errors, or null if the operation was successful.
     */
    public Map<String, String> getErrors() {
        return errors;
    }
}
