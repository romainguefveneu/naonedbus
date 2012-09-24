package net.naonedbus.bean.async;

public class AsyncResult<T> {
	private Exception exception;
	private T result;

	public void setException(Exception exception) {
		this.exception = exception;
	}

	public Exception getException() {
		return exception;
	}

	public void setResult(T result) {
		this.result = result;
	}

	public T getResult() {
		return result;
	}

}
