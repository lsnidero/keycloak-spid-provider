<#import "template.ftl" as layout>
<div class="col-sm-9 col-md-10 col-sm-push-3 col-md-push-2" data-ng-init="initSamlProvider()">
    <ol class="breadcrumb">
        <li><a href="#/realms/{{realm.realm}}/identity-provider-settings">{{:: 'identity-providers' | translate}}</a></li>
        <li data-ng-show="!newIdentityProvider && identityProvider.displayName">{{identityProvider.displayName}}</li>
        <li data-ng-show="!newIdentityProvider && !identityProvider.displayName">{{identityProvider.alias}}</li>
        <li data-ng-show="newIdentityProvider">{{:: 'add-identity-provider' | translate}}</li>
    </ol>

    <kc-tabs-identity-provider></kc-tabs-identity-provider>

    <form class="form-horizontal" name="realmForm" novalidate kc-read-only="!access.manageIdentityProviders">
        <fieldset>
            <div class="form-group clearfix">
                <label class="col-md-2 control-label" for="idpEntityId">{{:: 'identity-provider.spid.idpEntityId' | translate}}</label>
                <div class="col-sm-6">
                    <input class="form-control" id="idpEntityId" type="text"  ng-model="identityProvider.config.idpEntityId"  readonly>
                </div>
            </div>
        </fieldset>
        <fieldset>
            <div class="form-group clearfix">
                <label class="col-md-2 control-label" for="redirectUri">{{:: 'redirect-uri' | translate}}</label>
                <div class="col-sm-6">
                    <input class="form-control" id="redirectUri" type="text" value="{{callbackUrl}}{{identityProvider.alias}}/endpoint" readonly kc-select-action="click">
                </div>
                <kc-tooltip>{{:: 'redirect-uri.tooltip' | translate}}</kc-tooltip>
            </div>
        </fieldset>
        <fieldset>
            <div class="form-group clearfix">
                <label class="col-md-2 control-label" for="identifier"><span class="required">*</span> {{:: 'alias' | translate}}</label>
                <div class="col-md-6">
                    <input kc-no-reserved-chars class="form-control" id="identifier" type="text" ng-model="identityProvider.alias" data-ng-readonly="!newIdentityProvider" required>
                </div>
                <kc-tooltip>{{:: 'identity-provider.alias.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group clearfix">
                <label class="col-md-2 control-label" for="displayName"> {{:: 'display-name' | translate}}</label>
                <div class="col-md-6">
                    <input class="form-control" id="displayName" type="text" ng-model="identityProvider.displayName">
                </div>
                <kc-tooltip>{{:: 'identity-provider.display-name.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group">
                <label class="col-md-2 control-label" for="enabled">{{:: 'enabled' | translate}}</label>
                <div class="col-md-6">
                    <input ng-model="identityProvider.enabled" id="enabled" onoffswitch on-text="{{:: 'onText' | translate}}" off-text="{{:: 'offText' | translate}}" />
                </div>
                <kc-tooltip>{{:: 'identity-provider.enabled.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group">
                <label class="col-md-2 control-label" for="storeToken">{{:: 'store-tokens' | translate}}</label>
                <div class="col-md-6">
                    <input ng-model="identityProvider.storeToken" id="storeToken" onoffswitch on-text="{{:: 'onText' | translate}}" off-text="{{:: 'offText' | translate}}" />
                </div>
                <kc-tooltip>{{:: 'identity-provider.store-tokens.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group">
                <label class="col-md-2 control-label" for="storedTokensReadable">{{:: 'stored-tokens-readable' | translate}}</label>
                <div class="col-md-6">
                    <input ng-model="identityProvider.addReadTokenRoleOnCreate" id="storedTokensReadable" onoffswitch on-text="{{:: 'onText' | translate}}" off-text="{{:: 'offText' | translate}}" />
                </div>
                <kc-tooltip>{{:: 'identity-provider.stored-tokens-readable.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group">
                <label class="col-md-2 control-label" for="trustEmail">{{:: 'trust-email' | translate}}</label>
                <div class="col-md-6">
                    <input ng-model="identityProvider.trustEmail" name="identityProvider.trustEmail" id="trustEmail" onoffswitch on-text="{{:: 'onText' | translate}}" off-text="{{:: 'offText' | translate}}" />
                </div>
                <kc-tooltip>{{:: 'trust-email.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group">
                <label class="col-md-2 control-label" for="linkOnly">{{:: 'link-only' | translate}}</label>
                <div class="col-md-6">
                    <input ng-model="identityProvider.linkOnly" name="identityProvider.trustEmail" id="linkOnly" onoffswitch on-text="{{:: 'onText' | translate}}" off-text="{{:: 'offText' | translate}}" />
                </div>
                <kc-tooltip>{{:: 'link-only.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group">
                <label class="col-md-2 control-label" for="hideOnLoginPage">{{:: 'hide-on-login-page' | translate}}</label>
                <div class="col-md-6">
                    <input ng-model="identityProvider.config.hideOnLoginPage" name="identityProvider.config.hideOnLoginPage" id="hideOnLoginPage" onoffswitchvalue on-text="{{:: 'onText' | translate}}" off-text="{{:: 'offText' | translate}}" />
                </div>
                <kc-tooltip>{{:: 'hide-on-login-page.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group">
                <label class="col-md-2 control-label" for="guiOrder">{{:: 'gui-order' | translate}}</label>
                <div class="col-md-6">
                    <input class="form-control" id="guiOrder" type="text" ng-model="identityProvider.config.guiOrder">
                </div>
                <kc-tooltip>{{:: 'gui-order.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group">
                <label class="col-md-2 control-label" for="firstBrokerLoginFlowAlias">{{:: 'first-broker-login-flow' | translate}}</label>
                <div class="col-md-6">
                    <div>
                        <select class="form-control" id="firstBrokerLoginFlowAlias"
                                ng-model="identityProvider.firstBrokerLoginFlowAlias"
                                ng-options="flow.alias as flow.alias for flow in authFlows"
                                required>
                        </select>
                    </div>
                </div>
                <kc-tooltip>{{:: 'first-broker-login-flow.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group">
                <label class="col-md-2 control-label" for="postBrokerLoginFlowAlias">{{:: 'post-broker-login-flow' | translate}}</label>
                <div class="col-md-6">
                    <div>
                        <select class="form-control" id="postBrokerLoginFlowAlias"
                                ng-model="identityProvider.postBrokerLoginFlowAlias"
                                ng-options="flow.alias as flow.alias for flow in postBrokerAuthFlows">
                        </select>
                    </div>
                </div>
                <kc-tooltip>{{:: 'post-broker-login-flow.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group">
                <label class="col-md-2 control-label" for="syncMode">{{:: 'sync-mode' | translate}}</label>
                <div class="col-md-6">
                    <div>
                        <select class="form-control" id="syncMode"
                                ng-model="identityProvider.config.syncMode"
                                required>
                            <option id="syncMode_import" name="syncMode" value="IMPORT">{{:: 'sync-mode.import' | translate}}</option>
                            <option id="syncMode_legacy" name="syncMode" value="LEGACY">{{:: 'sync-mode.legacy' | translate}}</option>
                            <option id="syncMode_force" name="syncMode" value="FORCE">{{:: 'sync-mode.force' | translate}}</option>
                        </select>
                    </div>
                </div>
                <kc-tooltip>{{:: 'sync-mode.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group" data-ng-show="!importFile && !newIdentityProvider">
                <label class="col-md-2 control-label">{{:: 'endpoints' | translate}}</label>
                <div class="col-md-6">
                    <div>
                        <a class="form-control" ng-href="{{callbackUrl}}{{identityProvider.alias}}/endpoint/descriptor" target="_blank">{{:: 'identity-provider.saml.protocol-endpoints.saml' | translate}}</a>
                    </div>
                    <div>
                        <a class="form-control" ng-href="{{authUrl}}/realms/{{realm.realm}}/spid-sp-metadata" target="_blank"
                            title="{{:: 'identity-provider.spid.protocol-endpoints.saml.tooltip' | translate}}">{{:: 'identity-provider.spid.protocol-endpoints.saml' | translate}}</a>
                    </div>
                </div>
                <kc-tooltip>{{:: 'identity-provider.saml.protocol-endpoints.saml.tooltip' | translate}}</kc-tooltip>
            </div>
        </fieldset>
        <fieldset>
            <legend uncollapsed><span class="text">{{:: 'saml-config' | translate}}</span> <kc-tooltip>{{:: 'identity-provider.saml-config.tooltip' | translate}}</kc-tooltip></legend>

            <div class="form-group clearfix">
                <label class="col-md-2 control-label" for="entityId"><span class="required">*</span> {{:: 'identity-provider.saml.entity-id' | translate}}</label>
                <div class="col-md-6">
                    <input kc-no-reserved-chars class="form-control" id="entityId" type="text" ng-model="identityProvider.config.entityId" required>
                </div>
                <kc-tooltip>{{:: 'identity-provider.saml.entity-id.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group clearfix">
                <label class="col-md-2 control-label" for="singleSignOnServiceUrl"><span class="required">*</span> {{:: 'single-signon-service-url' | translate}}</label>
                <div class="col-md-6">
                    <input class="form-control" id="singleSignOnServiceUrl" type="text" ng-model="identityProvider.config.singleSignOnServiceUrl" required>
                </div>
                <kc-tooltip>{{:: 'saml.single-signon-service-url.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group clearfix">
                <label class="col-md-2 control-label" for="singleSignOnServiceUrl">{{:: 'single-logout-service-url' | translate}}</label>
                <div class="col-md-6">
                    <input class="form-control" id="singleLogoutServiceUrl" type="text" ng-model="identityProvider.config.singleLogoutServiceUrl">
                </div>
                <kc-tooltip>{{:: 'saml.single-logout-service-url.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group">
                <label class="col-sm-2 control-label" for="backchannelSupported">{{:: 'backchannel-logout' | translate}}</label>
                <div class="col-sm-4">
                    <input ng-model="identityProvider.config.backchannelSupported" id="backchannelSupported" onoffswitchvalue on-text="{{:: 'onText' | translate}}" off-text="{{:: 'offText' | translate}}" />
                </div>
                <kc-tooltip>{{:: 'backchannel-logout.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group clearfix">
                <label class="col-md-2 control-label" for="nameIDPolicyFormat">{{:: 'nameid-policy-format' | translate}}</label>
                <div class="col-md-6">
                    <select id="nameIDPolicyFormat" ng-model="identityProvider.config.nameIDPolicyFormat"
                            ng-options="nameFormat.format as nameFormat.name for nameFormat in nameIdFormats">
                    </select>
                    <!-- <input class="form-control" id="nameIDPolicyFormat" type="text" ng-model="identityProvider.config.nameIDPolicyFormat"> -->
                </div>
                <kc-tooltip>{{:: 'nameid-policy-format.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group clearfix">
                <label class="col-md-2 control-label" for="principalType">{{:: 'saml.principal-type' | translate}}</label>
                <div class="col-md-6">
                    <select id="principalType" ng-model="identityProvider.config.principalType"
                            ng-options="pType.type as pType.name for pType in principalTypes">
                    </select>
                </div>
                <kc-tooltip>{{:: 'saml.principal-type.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group clearfix" data-ng-show="identityProvider.config.principalType.endsWith('ATTRIBUTE')">
                <label class="col-md-2 control-label" for="principalAttribute">{{:: 'saml.principal-attribute' | translate}}</label>
                <div class="col-md-6">
                    <input class="form-control" id="principalAttribute" type="text" ng-model="identityProvider.config.principalAttribute" ng-required="identityProvider.config.principalType.endsWith('ATTRIBUTE')">
                </div>
                <kc-tooltip>{{:: 'saml.principal-attribute.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group">
                <label class="col-md-2 control-label" for="allowCreate">{{:: 'saml.allow-create' | translate}}</label>
                <div class="col-md-6">
                    <input ng-model="identityProvider.config.allowCreate" id="allowCreate" onoffswitchvalue on-text="{{:: 'onText' | translate}}" off-text="{{:: 'offText' | translate}}" />
                </div>
                <kc-tooltip>{{:: 'saml.allow-create.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group">
                <label class="col-md-2 control-label" for="postBindingResponse">{{:: 'http-post-binding-response' | translate}}</label>
                <div class="col-md-6">
                    <input ng-model="identityProvider.config.postBindingResponse" id="postBindingResponse" onoffswitchvalue on-text="{{:: 'onText' | translate}}" off-text="{{:: 'offText' | translate}}" />
                </div>
                <kc-tooltip>{{:: 'http-post-binding-response.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group">
                <label class="col-md-2 control-label" for="postBindingAuthnRequest">{{:: 'http-post-binding-for-authn-request' | translate}}</label>
                <div class="col-md-6">
                    <input ng-model="identityProvider.config.postBindingAuthnRequest" id="postBindingAuthnRequest" onoffswitchvalue on-text="{{:: 'onText' | translate}}" off-text="{{:: 'offText' | translate}}" />
                </div>
                <kc-tooltip>{{:: 'http-post-binding-for-authn-request.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group">
                <label class="col-md-2 control-label" for="postBindingLogout">{{:: 'http-post-binding-logout' | translate}}</label>
                <div class="col-md-6">
                    <input ng-model="identityProvider.config.postBindingLogout" id="postBindingLogout" onoffswitchvalue on-text="{{:: 'onText' | translate}}" off-text="{{:: 'offText' | translate}}" />
                </div>
                <kc-tooltip>{{:: 'http-post-binding-logout.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group">
                <label class="col-md-2 control-label" for="wantAuthnRequestsSigned">{{:: 'want-authn-requests-signed' | translate}}</label>
                <div class="col-md-6">
                    <input ng-model="identityProvider.config.wantAuthnRequestsSigned" id="wantAuthnRequestsSigned" name="wantAuthnRequestsSigned" onoffswitchvalue on-text="{{:: 'onText' | translate}}" off-text="{{:: 'offText' | translate}}" />
                </div>
                <kc-tooltip>{{:: 'want-authn-requests-signed.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group">
                <label class="col-md-2 control-label" for="wantAssertionsSigned">{{:: 'want-assertions-signed' | translate}}</label>
                <div class="col-md-6">
                    <input ng-model="identityProvider.config.wantAssertionsSigned" id="wantAssertionsSigned" name="wantAssertionsSigned" onoffswitchvalue on-text="{{:: 'onText' | translate}}" off-text="{{:: 'offText' | translate}}" />
                </div>
                <kc-tooltip>{{:: 'want-assertions-signed.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group">
                <label class="col-md-2 control-label" for="wantAssertionsEncrypted">{{:: 'want-assertions-encrypted' | translate}}</label>
                <div class="col-md-6">
                    <input ng-model="identityProvider.config.wantAssertionsEncrypted" id="wantAssertionsEncrypted" name="wantAssertionsEncrypted" onoffswitchvalue on-text="{{:: 'onText' | translate}}" off-text="{{:: 'offText' | translate}}" />
                </div>
                <kc-tooltip>{{:: 'want-assertions-encrypted.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group" data-ng-show="identityProvider.config.wantAuthnRequestsSigned == 'true'">
                <label class="col-md-2 control-label" for="signatureAlgorithm">{{:: 'signature-algorithm' | translate}}</label>
                <div class="col-sm-6">
                    <div>
                        <select class="form-control" id="signatureAlgorithm"
                                ng-model="identityProvider.config.signatureAlgorithm"
                                ng-options="alg for alg in signatureAlgorithms">
                        </select>
                    </div>
                </div>
                <kc-tooltip>{{:: 'signature-algorithm.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group clearfix block" data-ng-show="identityProvider.config.wantAuthnRequestsSigned == 'true'">
                <label class="col-md-2 control-label" for="samlSigKeyNameTranformer">{{:: 'saml-signature-keyName-transformer' | translate}}</label>
                <div class="col-md-6">
                    <div>
                        <select class="form-control" id="samlSigKeyNameTranformer"
                                ng-model="identityProvider.config.xmlSigKeyInfoKeyNameTransformer"
                                ng-options="xmlKeyNameTranformer for xmlKeyNameTranformer in xmlKeyNameTranformers">
                        </select>
                    </div>
                </div>
                <kc-tooltip>{{:: 'saml-signature-keyName-transformer.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group">
                <label class="col-md-2 control-label" for="forceAuthn">{{:: 'force-authentication' | translate}}</label>
                <div class="col-md-6">
                    <input ng-model="identityProvider.config.forceAuthn" id="forceAuthn" name="forceAuthn" onoffswitchvalue on-text="{{:: 'onText' | translate}}" off-text="{{:: 'offText' | translate}}" />
                </div>
                <kc-tooltip>{{:: 'identity-provider.force-authentication.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group">
                <label class="col-md-2 control-label" for="validateSignature">{{:: 'validate-signature' | translate}}</label>
                <div class="col-md-6">
                    <input ng-model="identityProvider.config.validateSignature" id="validateSignature" onoffswitchvalue on-text="{{:: 'onText' | translate}}" off-text="{{:: 'offText' | translate}}" />
                </div>
                <kc-tooltip>{{:: 'saml.validate-signature.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group clearfix" data-ng-show="identityProvider.config.validateSignature == 'true'">
                <label class="col-md-2 control-label" for="signingCertificate">{{:: 'validating-x509-certificate' | translate}}</label>
                <div class="col-md-6">
                    <textarea class="form-control" id="signingCertificate" ng-model="identityProvider.config.signingCertificate"></textarea>
                </div>
                <kc-tooltip>{{:: 'validating-x509-certificate.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group">
                <label class="col-md-2 control-label" for="signSpMetadata">{{:: 'identity-provider.saml.sign-sp-metadata' | translate}}</label>
                <div class="col-md-6">
                    <input ng-model="identityProvider.config.signSpMetadata" id="signSpMetadata" onoffswitchvalue on-text="{{:: 'onText' | translate}}" off-text="{{:: 'offText' | translate}}" />
                </div>
                <kc-tooltip>{{:: 'identity-provider.saml.sign-sp-metadata.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group">
                <label class="col-sm-2 control-label" for="loginHint">{{:: 'saml.loginHint' | translate}}</label>
                <div class="col-sm-4">
                    <input ng-model="identityProvider.config.loginHint" id="loginHint" onoffswitchvalue on-text="{{:: 'onText' | translate}}" off-text="{{:: 'offText' | translate}}" />
                </div>
                <kc-tooltip>{{:: 'saml.loginHint.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group">
                <label class="col-md-2 control-label" for="allowedClockSkew">{{:: 'allowed-clock-skew' | translate}}</label>
                <div class="col-md-6 time-selector">
                    <input class="form-control" string-to-number type="number" min="0" max="2147483" step="1" ng-model="identityProvider.config.allowedClockSkew" id="allowedClockSkew"/>
                </div>
                <kc-tooltip>{{:: 'identity-provider.allowed-clock-skew.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group">
                <label class="col-md-2 control-label" for="attributeConsumingServiceIndex">{{:: 'identity-provider.saml.attribute-consuming-service-index' | translate}}</label>
                <div class="col-md-6">
                    <input class="form-control" string-to-number type="number" min="0" max="2147483" step="1" ng-model="identityProvider.config.attributeConsumingServiceIndex" id="attributeConsumingServiceIndex"/>
                </div>
                <kc-tooltip>{{:: 'identity-provider.saml.attribute-consuming-service-index.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group">
                <label class="col-md-2 control-label" for="attributeConsumingServiceName">{{:: 'identity-provider.saml.attribute-consuming-service-name' | translate}}</label>
                <div class="col-md-6">
                    <input class="form-control" type="text" ng-model="identityProvider.config.attributeConsumingServiceName" id="attributeConsumingServiceName"/>
                </div>
                <kc-tooltip>{{:: 'identity-provider.saml.attribute-consuming-service-name.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group">
                <label class="col-md-2 control-label" for="organizationNames">{{:: 'identity-provider.spid.organization-names' | translate}}</label>
                <div class="col-md-6">
                    <input class="form-control" id="organizationNames" type="text" ng-model="identityProvider.config.organizationNames"/>
                </div>
                <kc-tooltip>{{:: 'identity-provider.spid.organization-names.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group">
                <label class="col-md-2 control-label" for="organizationDisplayNames">{{:: 'identity-provider.spid.organization-display-names' | translate}}</label>
                <div class="col-md-6">
                    <input class="form-control" id="organizationDisplayNames" type="text" ng-model="identityProvider.config.organizationDisplayNames"/>
                </div>
                <kc-tooltip>{{:: 'identity-provider.spid.organization-display-names.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group">
                <label class="col-md-2 control-label" for="organizationUrls">{{:: 'identity-provider.spid.organization-urls' | translate}}</label>
                <div class="col-md-6">
                    <input class="form-control" id="organizationUrls" type="text" ng-model="identityProvider.config.organizationUrls"/>
                </div>
                <kc-tooltip>{{:: 'identity-provider.spid.organization-urls.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group">
                <label class="col-md-2 control-label" for="isSpPrivate">{{:: 'identity-provider.spid.is-sp-private' | translate}}</label>
                <div class="col-md-6">
                    <input ng-model="identityProvider.config.otherContactIsSpPrivate" id="isSpPrivate" name="isSpPrivate" onoffswitchvalue on-text="{{:: 'onText' | translate}}" off-text="{{:: 'offText' | translate}}" />
                </div>
                <kc-tooltip>{{:: 'identity-provider.spid.is-sp-private.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group" data-ng-show="identityProvider.config.otherContactIsSpPrivate != 'true'">
                <label class="col-md-2 control-label" for="ipaCode">{{:: 'identity-provider.spid.ipaCode' | translate}}</label>
                <div class="col-md-6">
                    <input class="form-control" id="ipaCode" type="text" ng-model="identityProvider.config.otherContactIpaCode"/>
                </div>
                <kc-tooltip>{{:: 'identity-provider.spid.ipaCode.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group" data-ng-show="identityProvider.config.otherContactIsSpPrivate == 'true'">
                <label class="col-md-2 control-label" for="vatNumber">{{:: 'identity-provider.spid.vatNumber' | translate}}</label>
                <div class="col-md-6">
                    <input class="form-control" id="vatNumber" type="text" ng-model="identityProvider.config.otherContactVatNumber"/>
                </div>
                <kc-tooltip>{{:: 'identity-provider.spid.vatNumber.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group" data-ng-show="identityProvider.config.otherContactIsSpPrivate == 'true'">
                <label class="col-md-2 control-label" for="fiscalCode">{{:: 'identity-provider.spid.fiscalCode' | translate}}</label>
                <div class="col-md-6">
                    <input class="form-control" id="fiscalCode" type="text" ng-model="identityProvider.config.otherContactFiscalCode"/>
                </div>
                <kc-tooltip>{{:: 'identity-provider.spid.fiscalCode.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group">
                <label class="col-md-2 control-label" for="otherContactCompany">{{:: 'identity-provider.spid.contactCompany.other' | translate}}</label>
                <div class="col-md-6">
                    <input class="form-control" id="otherContactCompany" type="text" ng-model="identityProvider.config.otherContactCompany"/>
                </div>
                <kc-tooltip>{{:: 'identity-provider.spid.contactCompany.other.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group">
                <label class="col-md-2 control-label" for="otherContactPhone">{{:: 'identity-provider.spid.contactPhone.other' | translate}}</label>
                <div class="col-md-6">
                    <input class="form-control" id="otherContactPhone" type="text" ng-model="identityProvider.config.otherContactPhone"/>
                </div>
                <kc-tooltip>{{:: 'identity-provider.spid.contactPhone.other.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group">
                <label class="col-md-2 control-label" for="otherContactEmail">{{:: 'identity-provider.spid.contactEmail.other' | translate}}</label>
                <div class="col-md-6">
                    <input class="form-control" id="otherContactEmail" type="text" ng-model="identityProvider.config.otherContactEmail"/>
                </div>
                <kc-tooltip>{{:: 'identity-provider.spid.contactEmail.other.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group">
                <label class="col-md-2 control-label" for="billingContactCompany">{{:: 'identity-provider.spid.contactCompany.billing' | translate}}</label>
                <div class="col-md-6">
                    <input class="form-control" id="billingContactCompany" type="text" ng-model="identityProvider.config.billingContactCompany"/>
                </div>
                <kc-tooltip>{{:: 'identity-provider.spid.contactCompany.billing.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group">
                <label class="col-md-2 control-label" for="billingContactPhone">{{:: 'identity-provider.spid.contactPhone.billing' | translate}}</label>
                <div class="col-md-6">
                    <input class="form-control" id="billingContactPhone" type="text" ng-model="identityProvider.config.billingContactPhone"/>
                </div>
                <kc-tooltip>{{:: 'identity-provider.spid.contactPhone.billing.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group">
                <label class="col-md-2 control-label" for="billingContactEmail">{{:: 'identity-provider.spid.contactEmail.billing' | translate}}</label>
                <div class="col-md-6">
                    <input class="form-control" id="billingContactEmail" type="text" ng-model="identityProvider.config.billingContactEmail"/>
                </div>
                <kc-tooltip>{{:: 'identity-provider.spid.contactEmail.billing.tooltip' | translate}}</kc-tooltip>
            </div>
            <!-- Init Cessionario Committente -->
            <fieldset data-ng-show="identityProvider.config.otherContactIsSpPrivate == 'true'">
                <legend uncollapsed><span class="text">{{:: 'identity-provider.spid.cessionarioCommittente' | translate}}</span>
                    <kc-tooltip>{{:: 'identity-provider.spid.cessionarioCommittente.tooltip' | translate}}</kc-tooltip>
                </legend>
                <fieldset>
                    <legend uncollapsed><span class="text">{{:: 'identity-provider.spid.cessionarioCommittente.datiAnagrafici' |
                            translate}}</span>
                        <kc-tooltip>{{::'identity-provider.spid.cessionarioCommittente.datiAnagrafici.tooltip' |
                            translate}}</kc-tooltip>
                    </legend>
                    <fieldset>
                        <legend uncollapsed><span class="text">{{::
                                'identity-provider.spid.cessionarioCommittente.datiAnagrafici.idFiscaleIva' | translate}}</span>
                            <kc-tooltip>{{::'identity-provider.spid.cessionarioCommittente.datiAnagrafici.idFiscaleIva.tooltip' |
                                translate}}</kc-tooltip>
                        </legend>
                        <div class="form-group">
                            <label class="col-md-2 control-label" for="billingIdPaese">{{::
                                'identity-provider.spid.cessionarioCommittente.idPaese.billing' | translate}}</label>
                            <div class="col-md-6">
                                <input class="form-control" id="billingIdPaese" type="text"
                                    ng-model="identityProvider.config.billingIdPaese" />
                            </div>
                            <kc-tooltip>{{:: 'identity-provider.spid.cessionarioCommittente.idPaese.billing.tooltip' |
                                translate}}</kc-tooltip>
                        </div>
            
                        <div class="form-group">
                            <label class="col-md-2 control-label" for="billingIdCodice">{{::
                                'identity-provider.spid.cessionarioCommittente.idCodice.billing' | translate}}</label>
                            <div class="col-md-6">
                                <input class="form-control" id="billingIdCodice" type="text"
                                    ng-model="identityProvider.config.billingIdCodice" />
                            </div>
                            <kc-tooltip>{{:: 'identity-provider.spid.cessionarioCommittente.idCodice.billing.tooltip' |
                                translate}}</kc-tooltip>
                        </div>
                    </fieldset>
                    <div class="form-group">
                        <label class="col-md-2 control-label" for="billingCodiceFiscale">{{::
                            'identity-provider.spid.cessionarioCommittente.codiceFiscale.billing' | translate}}</label>
                        <div class="col-md-6">
                            <input class="form-control" id="billingCodiceFiscale" type="text"
                                ng-model="identityProvider.config.billingCodiceFiscale" />
                        </div>
                        <kc-tooltip>{{:: 'identity-provider.spid.cessionarioCommittente.codiceFiscale.billing.tooltip' |
                            translate}}</kc-tooltip>
                    </div>
                    <fieldset>
                        <legend uncollapsed><span class="text">{{:: 'identity-provider.spid.cessionarioCommittente.anagrafica' |
                                translate}}</span>
                            <kc-tooltip>{{::'identity-provider.spid.cessionarioCommittente.anagrafica.tooltip' |
                                translate}}</kc-tooltip>
                        </legend>
                        <div class="form-group">
                            <label class="col-md-2 control-label" for="billingAnagraficaDenominazione">{{::
                                'identity-provider.spid.cessionarioCommittente.anagraficaDenominazione.billing' |
                                translate}}</label>
                            <div class="col-md-6">
                                <input class="form-control" id="billingAnagraficaDenominazione" type="text"
                                    ng-model="identityProvider.config.billingAnagraficaDenominazione" />
                            </div>
                            <kc-tooltip>{{:: 'identity-provider.spid.cessionarioCommittente.anagraficaDenominazione.billing.tooltip'
                                |
                                translate}}</kc-tooltip>
                        </div>
            
                        <div class="form-group">
                            <label class="col-md-2 control-label" for="billingAnagraficaNome">{{::
                                'identity-provider.spid.cessionarioCommittente.anagraficaNome.billing' | translate}}</label>
                            <div class="col-md-6">
                                <input class="form-control" id="billingAnagraficaNome" type="text"
                                    ng-model="identityProvider.config.billingAnagraficaNome" />
                            </div>
                            <kc-tooltip>{{:: 'identity-provider.spid.cessionarioCommittente.anagraficaNome.billing.tooltip' |
                                translate}}</kc-tooltip>
                        </div>
            
                        <div class="form-group">
                            <label class="col-md-2 control-label" for="billingAnagraficaCognome">{{::
                                'identity-provider.spid.cessionarioCommittente.anagraficaCognome.billing' | translate}}</label>
                            <div class="col-md-6">
                                <input class="form-control" id="billingAnagraficaCognome" type="text"
                                    ng-model="identityProvider.config.billingAnagraficaCognome" />
                            </div>
                            <kc-tooltip>{{:: 'identity-provider.spid.cessionarioCommittente.anagraficaCognome.billing.tooltip' |
                                translate}}</kc-tooltip>
                        </div>
            
                        <div class="form-group">
                            <label class="col-md-2 control-label" for="billingAnagraficaTitolo">{{::
                                'identity-provider.spid.cessionarioCommittente.anagraficaTitolo.billing' | translate}}</label>
                            <div class="col-md-6">
                                <input class="form-control" id="billingAnagraficaTitolo" type="text"
                                    ng-model="identityProvider.config.billingAnagraficaTitolo" />
                            </div>
                            <kc-tooltip>{{:: 'identity-provider.spid.cessionarioCommittente.anagraficaTitolo.billing.tooltip' |
                                translate}}</kc-tooltip>
                        </div>
            
                        <div class="form-group">
                            <label class="col-md-2 control-label" for="billingAnagraficaCodiceEORI">{{::
                                'identity-provider.spid.cessionarioCommittente.anagraficaCodiceEORI.billing' | translate}}</label>
                            <div class="col-md-6">
                                <input class="form-control" id="billingAnagraficaCodiceEORI" type="text"
                                    ng-model="identityProvider.config.billingAnagraficaCodiceEORI" />
                            </div>
                            <kc-tooltip>{{:: 'identity-provider.spid.cessionarioCommittente.anagraficaCodiceEORI.billing.tooltip' |
                                translate}}</kc-tooltip>
                        </div>
                    </fieldset>
                </fieldset>
                <fieldset>
                    <legend uncollapsed><span class="text">{{:: 'identity-provider.spid.cessionarioCommittente.sede' |
                            translate}}</span>
                        <kc-tooltip>{{::'identity-provider.spid.cessionarioCommittente.sede.tooltip' | translate}}</kc-tooltip>
                    </legend>
                    <div class="form-group">
                        <label class="col-md-2 control-label" for="billingSedeIndirizzo">{{::
                            'identity-provider.spid.cessionarioCommittente.sedeIndirizzo.billing' | translate}}</label>
                        <div class="col-md-6">
                            <input class="form-control" id="billingSedeIndirizzo" type="text"
                                ng-model="identityProvider.config.billingSedeIndirizzo" />
                        </div>
                        <kc-tooltip>{{:: 'identity-provider.spid.cessionarioCommittente.sedeIndirizzo.billing.tooltip' |
                            translate}}</kc-tooltip>
                    </div>
            
                    <div class="form-group">
                        <label class="col-md-2 control-label" for="billingSedeNumeroCivico">{{::
                            'identity-provider.spid.cessionarioCommittente.sedeNumeroCivico.billing' | translate}}</label>
                        <div class="col-md-6">
                            <input class="form-control" id="billingSedeNumeroCivico" type="text"
                                ng-model="identityProvider.config.billingSedeNumeroCivico" />
                        </div>
                        <kc-tooltip>{{:: 'identity-provider.spid.cessionarioCommittente.sedeNumeroCivico.billing.tooltip' |
                            translate}}</kc-tooltip>
                    </div>
            
                    <div class="form-group">
                        <label class="col-md-2 control-label" for="billingSedeCap">{{::
                            'identity-provider.spid.cessionarioCommittente.sedeCap.billing' | translate}}</label>
                        <div class="col-md-6">
                            <input class="form-control" id="billingSedeCap" type="text"
                                ng-model="identityProvider.config.billingSedeCap" />
                        </div>
                        <kc-tooltip>{{:: 'identity-provider.spid.cessionarioCommittente.sedeCap.billing.tooltip' |
                            translate}}</kc-tooltip>
                    </div>
            
                    <div class="form-group">
                        <label class="col-md-2 control-label" for="billingSedeComune">{{::
                            'identity-provider.spid.cessionarioCommittente.sedeComune.billing' | translate}}</label>
                        <div class="col-md-6">
                            <input class="form-control" id="billingSedeComune" type="text"
                                ng-model="identityProvider.config.billingSedeComune" />
                        </div>
                        <kc-tooltip>{{:: 'identity-provider.spid.cessionarioCommittente.sedeComune.billing.tooltip' |
                            translate}}</kc-tooltip>
                    </div>
            
                    <div class="form-group">
                        <label class="col-md-2 control-label" for="billingSedeProvincia">{{::
                            'identity-provider.spid.cessionarioCommittente.sedeProvincia.billing' | translate}}</label>
                        <div class="col-md-6">
                            <input class="form-control" id="billingSedeProvincia" type="text"
                                ng-model="identityProvider.config.billingSedeProvincia" />
                        </div>
                        <kc-tooltip>{{:: 'identity-provider.spid.cessionarioCommittente.sedeProvincia.billing.tooltip' |
                            translate}}</kc-tooltip>
                    </div>
            
                    <div class="form-group">
                        <label class="col-md-2 control-label" for="billingSedeNazione">{{::
                            'identity-provider.spid.cessionarioCommittente.sedeNazione.billing' | translate}}</label>
                        <div class="col-md-6">
                            <input class="form-control" id="billingSedeNazione" type="text"
                                ng-model="identityProvider.config.billingSedeNazione" />
                        </div>
                        <kc-tooltip>{{:: 'identity-provider.spid.cessionarioCommittente.sedeNazione.billing.tooltip' |
                            translate}}</kc-tooltip>
                    </div>
                </fieldset>
                <!-- End Cessionario Committente -->
                <fieldset>
                    <legend uncollapsed><span class="text">{{:: 'identity-provider.spid.terzoIntermediario.soggettoEmittente' |
                            translate}}</span> <kc-tooltip>{{::
                            'identity-provider.spid.terzoIntermediario.soggettoEmittente.tooltip' | translate}}</kc-tooltip>
                    </legend>
                    <div class="form-group">
                        <label class="col-md-2 control-label" for="billingTerzoIntermediarioSoggettoEmittente">{{::
                            'identity-provider.spid.terzoIntermediario.soggettoEmittente.billing' | translate}}</label>
                        <div class="col-md-6">
                            <input class="form-control" id="billingTerzoIntermediarioSoggettoEmittente" type="text"
                                ng-model="identityProvider.config.billingTerzoIntermediarioSoggettoEmittente" />
                        </div>
                        <kc-tooltip>{{:: 'identity-provider.spid.terzoIntermediario.soggettoEmittente.billing.tooltip' |
                            translate}}</kc-tooltip>
                    </div>
                </fieldset>
            </fieldset>
        </fieldset>
        <fieldset>
            <legend collapsed><span class="text">{{:: 'identity-provider.saml.requested-authncontext' | translate}}</span> <kc-tooltip>{{:: 'identity-provider.saml.requested-authncontext.tooltip' | translate}}</kc-tooltip></legend>

            <div class="form-group clearfix">
                <label class="col-md-2 control-label" for="authnContextComparisonType">{{:: 'identity-provider.saml.authncontext-comparison-type' | translate}}</label>
                <div class="col-md-6">
                    <div>
                        <select class="form-control" id="authnContextComparisonType"
                                ng-init="identityProvider.config.authnContextComparisonType = identityProvider.config.authnContextComparisonType || 'exact'"
                                ng-model="identityProvider.config.authnContextComparisonType">
                            <option value="exact">{{:: 'identity-provider.saml.authncontext-comparison-type.exact' | translate}}</option>
                            <option value="minimum">{{:: 'identity-provider.saml.authncontext-comparison-type.minimum' | translate}}</option>
                            <option value="maximum">{{:: 'identity-provider.saml.authncontext-comparison-type.maximum' | translate}}</option>
                            <option value="better">{{:: 'identity-provider.saml.authncontext-comparison-type.better' | translate}}</option>
                        </select>
                    </div>
                </div>
                <kc-tooltip>{{:: 'identity-provider.saml.authncontext-comparison-type.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group">
                <label for="type" class="col-md-2 control-label"><span class="required">*</span>{{:: 'identity-provider.saml.authncontext-class-ref' | translate}}</label>
                <div class="col-md-6">
                    <div>
                        <select class="form-control" id="authnContextClassRefs"
                        ng-init="identityProvider.config.authnContextClassRefs = identityProvider.config.authnContextClassRefs"
                        ng-model="identityProvider.config.authnContextClassRefs" required>
                            <option value='["https://www.spid.gov.it/SpidL1"]'>https://www.spid.gov.it/SpidL1</option>
                            <option value='["https://www.spid.gov.it/SpidL2"]'>https://www.spid.gov.it/SpidL2</option>
                            <option value='["https://www.spid.gov.it/SpidL3"]'>https://www.spid.gov.it/SpidL3</option>
                        </select>
                    </div>
                </div>
                <kc-tooltip>{{:: 'identity-provider.saml.authncontext-class-ref.tooltip' | translate}}</kc-tooltip>
                    <!-- authnContextClassRef is a List. SPID wants only a string. -->
                    <!--  this is why this code is commented
                    <div class="input-group" ng-repeat="(i, authnContextClassRef) in authnContextClassRefs track by $index">
                        <input class="form-control" ng-model="authnContextClassRefs[i]">
                        <div class="input-group-btn">
                            <button class="btn btn-default" type="button" data-ng-click="deleteAuthnContextClassRef($index)">
                                <span class="fa fa-minus"></span>
                            </button>
                        </div>
                    </div>
                    <div class = "input-group">
                        <input class="form-control" ng-model="newAuthnContextClassRef" id="newAuthnContextClassRef">
                        <div class="input-group-btn">
                            <button class="btn btn-default" type="button" data-ng-click="newAuthnContextClassRef.length > 0 && addAuthnContextClassRef()">
                                <span class="fa fa-plus"></span>
                            </button>
                        </div>
                    </div>
                    
                </div>
                <kc-tooltip>{{:: 'identity-provider.saml.authncontext-class-ref.tooltip' | translate}}</kc-tooltip>
                -->
            </div>
            <div class="form-group">
                <label for="type" class="col-md-2 control-label">{{:: 'identity-provider.saml.authncontext-decl-ref' | translate}}</label>
                <div class="col-sm-4">
                    <div class="input-group" ng-repeat="(i, authnContextDeclRef) in authnContextDeclRefs track by $index">
                        <input class="form-control" ng-model="authnContextDeclRefs[i]">
                        <div class="input-group-btn">
                            <button class="btn btn-default" type="button" data-ng-click="deleteAuthnContextDeclRef($index)">
                                <span class="fa fa-minus"></span>
                            </button>
                        </div>
                    </div>
                    <div class = "input-group">
                        <input class="form-control" ng-model="newAuthnContextDeclRef" id="newAuthnContextDeclRef">
                        <div class="input-group-btn">
                            <button class="btn btn-default" type="button" data-ng-click="newAuthnContextDeclRef.length > 0 && addAuthnContextDeclRef()">
                                <span class="fa fa-plus"></span>
                            </button>
                        </div>
                    </div>
                </div>
                <kc-tooltip>{{:: 'identity-provider.saml.authncontext-decl-ref.tooltip' | translate}}</kc-tooltip>
            </div>
        </fieldset>
        <fieldset data-ng-show="newIdentityProvider">
            <legend uncollapsed><span class="text">{{:: 'import-external-idp-config' | translate}}</span> <kc-tooltip>{{:: 'import-external-idp-config.tooltip' | translate}}</kc-tooltip></legend>
            <div class="form-group" data-ng-show="newIdentityProvider">
                <label class="col-md-2 control-label" for="fromUrl">{{:: 'import-from-url' | translate}}</label>
                <div class="col-md-6">
                    <input class="form-control" id="fromUrl" type="text" ng-model="fromUrl.data">
                </div>
                <kc-tooltip>{{:: 'saml.import-from-url.tooltip' | translate}}</kc-tooltip>
            </div>
            <div class="form-group">
                <label class="col-md-2 control-label" for="importFrom"></label>
                <div class="col-md-6">
                    <button id="importFrom" type="button" data-ng-click="importFrom()" data-ng-show="importUrl" class="btn btn-primary">{{:: 'import' | translate}}</button>
                </div>
            </div>
            <div class="form-group" data-ng-show="newIdentityProvider">
                <label class="col-md-2 control-label">{{:: 'import-from-file' | translate}}</label>
                <div class="col-md-6">
                    <div class="controls kc-button-input-file" data-ng-show="!files || files.length == 0">
                        <label for="import-file" class="btn btn-default">{{:: 'select-file' | translate}} <i class="pficon pficon-import"></i></label>
                        <input id="import-file" type="file" class="hidden" ng-file-select="onFileSelect($files)">
                    </div>
                <span class="kc-uploaded-file" data-ng-show="files.length > 0">
                    {{files[0].name}}
                </span>
                </div>
                <div class="form-group">
                    <label class="col-md-2 control-label" for="importFile"></label>
                    <div class="col-sm-6" data-ng-show="importFile">
                        <button id="importFile" type="button" data-ng-click="uploadFile()" data-ng-show="importFile" class="btn btn-primary">{{:: 'import' | translate}}</button>
                    </div>
                </div>
            </div>
        </fieldset>

        <div class="form-group">
            <div class="col-md-10 col-md-offset-2">
                <button kc-save data-ng-disabled="!changed">{{:: 'save' | translate}}</button>
                <button kc-cancel data-ng-click="cancel()" data-ng-disabled="!changed">{{:: 'cancel' | translate}}</button>
            </div>
        </div>
    </form>
</div>

<kc-menu></kc-menu>