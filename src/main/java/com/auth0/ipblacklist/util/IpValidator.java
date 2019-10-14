package com.auth0.ipblacklist.util;

import com.auth0.ipblacklist.exception.ValidationException;

public class IpValidator {
  public static void validate(String ip) throws ValidationException {
    if (ip == null || ip.isBlank()) {
      throw new ValidationException("IP cannot be blank: " + ip);
    }
    if (ip.length() < 7 || ip.length() > 15
      || !ip.matches("^\\d+\\.\\d+\\.\\d+\\.\\d+$")) {
      throw new ValidationException("Invalid IP format, should be xxx.xxx.xxx.xxx: " + ip);
    }
  }
}
