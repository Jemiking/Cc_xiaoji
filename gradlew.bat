#!/bin/bash
# Wrapper script for MCP compatibility in WSL2
# This script allows MCP server to work correctly in WSL2 environment
exec ./gradlew "$@"