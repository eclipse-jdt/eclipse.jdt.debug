pipeline {
	options {
		timeout(time: 40, unit: 'MINUTES')
		buildDiscarder(logRotator(numToKeepStr:'15'))
		disableConcurrentBuilds(abortPrevious: true)
		timestamps()
	}
	agent {
		label "centos-latest"
	}
	tools {
		maven 'apache-maven-latest'
		jdk 'openjdk-jdk21-latest'
	}
	stages {
		stage('Build') {
			steps {
				wrap([$class: 'Xvnc', useXauthority: true]) {
					sh """
					mvn clean verify --batch-mode --fail-at-end -Dmaven.repo.local=$WORKSPACE/.m2/repository \
						-Pbuild-individual-bundles -Ptest-on-javase-21 -Pbree-libs -Papi-check\
						-Dcompare-version-with-baselines.skip=false \
						-Dproject.build.sourceEncoding=UTF-8 \
						-DDetectVMInstallationsJob.disabled=true \
						-Dtycho.apitools.debug \
						-DtrimStackTrace=false
					"""
				}
			}
			post {
				always {
					archiveArtifacts artifacts: '*.log,*/target/work/data/.metadata/*.log,*/tests/target/work/data/.metadata/*.log,apiAnalyzer-workspace/.metadata/*.log', allowEmptyArchive: true
					junit '**/target/surefire-reports/*.xml'
					discoverGitReferenceBuild referenceJob: 'eclipse.jdt.debug-github/master'
					recordIssues publishAllIssues:false, ignoreQualityGate:true, tool: eclipse(pattern: '**/target/compilelogs/*.xml'), qualityGates: [[threshold: 1, type: 'DELTA', unstable: false]]
					recordIssues publishAllIssues:false, ignoreQualityGate:true, tool: javaDoc(), qualityGates: [[threshold: 1, type: 'DELTA', unstable: false]]
					recordIssues publishAllIssues:false, ignoreQualityGate:true, tool: mavenConsole(), qualityGates: [[threshold: 1, type: 'DELTA_ERROR', unstable: false]]
				}
			}
		}
	}
}
