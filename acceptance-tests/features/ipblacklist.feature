@contract
Feature: IP Blacklist

  Scenario: Negative (IP not blacklisted)
    When I GET /ips/1.1.1.1
    Then response code should be 204

  Scenario: Positive (IP blacklisted)
    When I GET /ips/5.9.253.173
    Then response code should be 200
    And response body path $.blacklist should be firehol_level1.netset
    And response body path $.ip should be 5.9.253.173

  Scenario: Positive (subnet blacklisted)
    When I GET /ips/0.0.0.0
    Then response code should be 200
    And response body path $.blacklist should be firehol_level1.netset
    And response body path $.subnet should be 0.0.0.0/8

  Scenario: Low latency under load
    When I send 2000 requests to get IP
    Then maximum latency is below 200 ms and average latency is below 50 ms
