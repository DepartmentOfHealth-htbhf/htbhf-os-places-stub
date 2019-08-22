#!/bin/bash

check_variable_is_set(){
    if [[ -z ${!1} ]]; then
        echo "$1 must be set and non empty"
        exit 1
    fi
}

check_variable_is_set APP_NAME
check_variable_is_set BIN_DIR
check_variable_is_set GH_WRITE_TOKEN
check_variable_is_set CF_PUBLIC_DOMAIN
check_variable_is_set DEVELOPMENT_HOSTNAME
check_variable_is_set STAGING_HOSTNAME

# download the deployment script(s)
echo "Installing deploy scripts"
mkdir -p ${BIN_DIR}
rm -rf ${BIN_DIR}/deployment-scripts
mkdir ${BIN_DIR}/deployment-scripts

curl -s https://api.github.com/repos/DepartmentOfHealth-htbhf/htbhf-deployment-scripts/releases/latest \
| grep zipball_url \
| cut -d'"' -f4 \
| wget -qO deployment-scripts.zip -i -

unzip deployment-scripts.zip
mv -f DepartmentOfHealth-htbhf-htbhf-deployment-scripts-*/* ${BIN_DIR}/deployment-scripts
rm -rf DepartmentOfHealth-htbhf-htbhf-deployment-scripts-*
rm deployment-scripts.zip

export SCRIPT_DIR=${BIN_DIR}/deployment-scripts
export PATH=$PATH:${SCRIPT_DIR}

# determine APP_PATH
export APP_VERSION=`cat version.properties | grep "version" | cut -d'=' -f2`
export APP_PATH="build/libs/$APP_NAME-$APP_VERSION.jar"

# deploy to development and staging
export CF_SPACE=development
/bin/bash ${SCRIPT_DIR}/deploy.sh
cf map-route "${APP_NAME}-${CF_SPACE}" ${CF_PUBLIC_DOMAIN} --hostname ${DEVELOPMENT_HOSTNAME}

RESULT=$?

if [[ ${RESULT} != 0 ]]; then
  echo "# Deployment to development failed, skipping deployment to staging"
  echo "# See deployment logs below"
  cf logs ${APP_NAME}-${CF_SPACE} --recent
  exit 1
fi

export CF_SPACE=staging
/bin/bash ${SCRIPT_DIR}/deploy.sh
cf map-route "${APP_NAME}-${CF_SPACE}" ${CF_PUBLIC_DOMAIN} --hostname ${STAGING_HOSTNAME}
