#!/bin/sh

# ---------------------------------------------------------
# 1. 基础环境配置
# ---------------------------------------------------------
# 强制使用 GMT+8 时区，解决容器时间与宿主机不一致问题
DEFAULT_OPTS="-Duser.timezone=GMT+8 -Dfile.encoding=UTF-8 -Djava.awt.headless=true"

# ---------------------------------------------------------
# 2. 内存优化逻辑 (关键改进)
# ---------------------------------------------------------
# -XX:MaxRAMPercentage: 只有在没有手动指定 -Xmx 时才会生效
# 设置为 75.0 预留 25% 给非堆内存(Metaspace/Stack/DirectBuffer)，防止容器被 OOMKill
# -XX:+ExitOnOutOfMemoryError: 发生 OOM 时容器立即退出，由 K8s/Docker 重启，避免应用僵死
MEMORY_OPTS=" -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0 -XX:+ExitOnOutOfMemoryError"

# ---------------------------------------------------------
# 3. 外部插件 (Sky-walking 等)
# ---------------------------------------------------------
AGENT_OPTS=""
if [ -n "$AGENT_PATH" ] && [ -f "$AGENT_PATH" ]; then
  AGENT_OPTS="-javaagent:$AGENT_PATH"
  echo "[INFO] Sky-walking agent loaded from $AGENT_PATH"
else
  echo "[INFO] AGENT_PATH is empty or file not found, skipping agent."
fi

# ---------------------------------------------------------
# 4. 参数优先级处理
# ---------------------------------------------------------
# 注意：将 $CUSTOM_JAVA_OPTS 放在最后。
# 这样你在 docker run -e CUSTOM_JAVA_OPTS="-Xmx2g" 时，
# 后面的 -Xmx2g 会自动覆盖前面的 -XX:MaxRAMPercentage。
FINAL_JAVA_OPTS="$DEFAULT_OPTS $MEMORY_OPTS $AGENT_OPTS $CUSTOM_JAVA_OPTS"

echo "-------------------------------------------------------"
echo "  Project: information-portal"
echo "  Final JAVA_OPTS: $FINAL_JAVA_OPTS"
echo "  Active Profile: ${profile_name:-k8s}"
echo "-------------------------------------------------------"

# ---------------------------------------------------------
# 5. 启动程序
# ---------------------------------------------------------
# 使用 exec 确保 PID 1 身份，接收系统停止信号实现优雅停机
exec "$JAVA_HOME"/bin/java "$FINAL_JAVA_OPTS" \
  -Dspring.profiles.active="${profile_name:-k8s}" \
  -cp "./lib/*:spring-ai.jar" \
  net.platform.ai.SpringAiApplication