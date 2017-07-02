gradle_command='./gradlew'
java_command='java'
java_project_artifacts='anomaly-detection'
java_project_version='1.0'
java_jar_path="./build/libs/${java_project_artifacts}-${java_project_version}.jar"

if [ ! -f ${java_jar_path} ]; then
    echo "Building ${java_project_artifacts}" >&2
    ${gradle_command} build
fi

echo "Running ${java_project_artifacts}" >&2
${java_command} -jar ${java_jar_path}
