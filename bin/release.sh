#!/usr/bin/env bash
set -euo pipefail

sbt '+ publishSigned'
sbt '+ sonatypeRelease'
