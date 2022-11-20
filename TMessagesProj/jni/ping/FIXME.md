[ping.c](./ping.c#L61)
    Result of delta between t0 and t1 is 0 becase both t0 and t1 are 0.
    Need to check the call to clock_gettime().
