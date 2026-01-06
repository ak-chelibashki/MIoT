#!/bin/sh
set -eu

exec java -cp "/opt/jade/lib/*" jade.Boot \
  -local-host jade-platform \
  -port 1099
