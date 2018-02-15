# GPSSpeedFix

The GPS HAL in the Joying Sofia 3GR units uses integers instead of floats to calculate the meters/second. This give speed steps of 3.6 km/hr, or actually rounded to 3 or 4 km/hr steps.
This Xposed module calculates the right GPS speed and feeds it back to the "system", so that all dependent apps like navigation apps or GPS trackers can use the correct speed.

The repository is maintained here to make it easier to change and develop (or actually deviate) when necessary.


### Changelog:
* 11 February 2018: Decrease number of samples from 5 to 3 to reduce the lag a bit.
* 07 February 2018: correct speed-lastbearing error (detected by user jvdv from forum.carjoying)
