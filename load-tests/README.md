# load-tests

Benchmark ip-blacklist.

## Run

Install jmeter.

Run, eg:

```shell script
/Applications/apache-jmeter-5.1.1/bin/jmeter -n -t ./ip-blacklist_test-plan.jmx -l log.jtl -e -o report
```

Results:
- HTML report: `report/index.html`.
- `log.jtl` contains results for each request performed.

## Tweak

Run jmeter in GUI mode and open `./ip-blacklist_test-plan.jmx`.