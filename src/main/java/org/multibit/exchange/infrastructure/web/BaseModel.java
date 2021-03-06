package org.multibit.exchange.infrastructure.web;

/**
 * <p>Base class to provide the following to views:</p>
 * <ul>
 * <li>Access to common data</li>
 *
 * @since 0.0.1
 *         
 */
public class BaseModel {

  private String message;

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
