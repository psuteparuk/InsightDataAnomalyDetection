gradle_command="./gradlew"
java_project_artifacts="anomaly-detection"
java_bin_path="./build/install/${java_project_artifacts}/bin"
java_command="${java_bin_path}/${java_project_artifacts}"

batch_file_path="./log_input/batch_log.json"
stream_file_path="./log_input/stream_log.json"
flagged_file_path="./log_output/flagged_purchases.json"

if [ ! -x ${java_command} ]; then
    echo "Building ${java_project_artifacts}" >&2
    ${gradle_command} installDist
fi

echo "Running ${java_project_artifacts}" >&2
${java_command} \
    --batch "${batch_file_path}" \
    --stream "${stream_file_path}" \
    --flagged "${flagged_file_path}"
