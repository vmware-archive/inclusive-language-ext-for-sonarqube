#!/bin/bash
# Copyright 2022 VMware, Inc.
# SPDX-License-Identifier: BSD-2
pwd
mkdir plugins
mvn package
cp target/vmw-its-scanner*.jar plugins
ls -lF plugins
