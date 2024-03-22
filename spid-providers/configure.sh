#!/bin/bash

# Configuration
KCADM=${1:-/opt/eap/bin/kcadm.sh}    # Location of kcadm.sh
KSERVER=${2:-http://localhost:8080}  # Server host:port
KTREALM=${3:-my-spid}                # Target Realm
CLEAN_UP=false                       # If set to true deletes every provider in the SP list

# List of Service Providers
IDP=(
"infocertid"
"namirialid"
"lepidaid"
"arubaid"
"etnaid"
"sielteid"
"infocamereid"
"timid"
"spiditaliaid"
"posteid"
"teamsystemid"
)

# Counter for server calls
ADMIN_CALLS=0

START=$(date +%s)

# Configure keycloak
${KCADM} config credentials --server ${KSERVER} --realm master --user admin
if [ ! $? -eq 0 ]; then
    exit 1
fi
(( ADMIN_CALLS++ ))

echo "##########################################################################################"
echo "# Location of kcadm.sh: ${KCADM}"
echo "# Server is at ${KSERVER}"
echo "# Target realm is ${KTREALM}"
if [ "${CLEAN_UP}" == true ]; then
    echo "#"
    echo "# WARNING: This session will delete every Identity provider of the list. "
    echo "# Press CTRL+C now if this was a mistake!" 
    echo "#"
fi
echo "##########################################################################################"
read -n1 -r -p "Press any key to continue..." key

for idp in "${IDP[@]}";
do
    if [ "${CLEAN_UP}" == true ]; then
        echo "Deleting Identity Provider with alias ${idp}"    
        ${KCADM} delete identity-provider/instances/${idp} -r ${KTREALM}
        (( ADMIN_CALLS++ ))
        continue
    fi
    echo "Creating Identity Provider with alias ${idp}"
    ${KCADM} create identity-provider/instances -r ${KTREALM} -f resources/idp/${idp}.json
    (( ADMIN_CALLS++ ))
    echo "Creating custom mappers for provider ${idp}"
    for file in resources/mappers/*.json; 
    do
        dest_file="/tmp/${idp}-${file##*/}"
        cat ${file} | sed "s/CHANGE-IT/${idp}/" > ${dest_file}
        ${KCADM} create identity-provider/instances/${idp}/mappers -r ${KTREALM} -f ${dest_file}
        (( ADMIN_CALLS++ ))
    done
done

END=$(date +%s)

echo "Done. Keycloak server received ${ADMIN_CALLS} http requests in about $((END-START)) seconds"

