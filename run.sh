#!/bin/bash
#  -e AGENT_PATH=/opt/skywalking/agent/skywalking-agent.jar \
docker run -d \
  --name information \
  -m 6g \
  --memory-swap 6g \
  -e profile_name=prod \
  -p 8080:8080 \
  your-image-name