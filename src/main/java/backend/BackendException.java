package backend;

public class BackendException extends Exception {
  public BackendException(String message, Exception e) {
    super(message, e);
  }
}
