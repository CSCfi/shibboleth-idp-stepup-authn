/*
 * The MIT License
 * Copyright (c) 2015 CSC - IT Center for Science, http://www.csc.fi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package fi.csc.idp.stepup.impl;

import java.security.Principal;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

import net.shibboleth.idp.authn.AbstractExtractionAction;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;

import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.csc.idp.stepup.api.StepUpAccount;
import fi.csc.idp.stepup.api.StepUpEventIds;
import fi.csc.idp.stepup.api.StepUpMethod;
import fi.csc.idp.stepup.api.StepUpMethodContext;

/**
 * An action that updates a account. This is a dummy version still.
 * 
 */
@SuppressWarnings("rawtypes")
public class UpdateAccount extends AbstractExtractionAction {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(UpdateAccount.class);

    /** update parameter. */
    private String updateParameter = "_eventId_update";

    /** proxy StepUp Context. */
    private StepUpMethodContext stepUpMethodContext;

    /**
     * Sets the parameter the response is read from.
     * 
     * @param parameter
     *            name for response
     */
    public void setUpdateParameter(@Nonnull @NotEmpty String parameter) {
        this.updateParameter = parameter;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    /** {@inheritDoc} */
    @Override
    protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {

        log.trace("Entering");
        stepUpMethodContext = authenticationContext.getSubcontext(StepUpMethodContext.class);
        if (stepUpMethodContext == null) {
            log.debug("{} Could not get shib proxy context", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EVENTID_MISSING_STEPUPMETHODCONTEXT);
            log.trace("Leaving");
            return false;
        }
        if (stepUpMethodContext.getStepUpAccount() == null) {
            log.debug("There is no chosen stepup account for user", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EVENTID_INVALID_USER);
            log.trace("Leaving");
            return false;
        }
        return super.doPreExecute(profileRequestContext, authenticationContext);
    }

    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {

        log.trace("Entering");
        final HttpServletRequest request = getHttpServletRequest();
        if (request == null) {
            log.debug("{} Profile action does not contain an HttpServletRequest", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EXCEPTION);
            log.trace("Leaving");
            return;
        }
        final String updateValue = request.getParameter(updateParameter);
        if (updateValue == null) {
            log.debug("No update value found", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EVENTID_INVALID_RESPONSE);
            log.trace("Leaving");
            return;
        }
        String updateCommand[] = updateValue.split(":");
        if (updateCommand.length != 3) {
            log.debug("{} the command should have 3 parts", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EXCEPTION);
            log.trace("Leaving");
            return;
        }
        String method = updateCommand[0];
        if (method == null || method.isEmpty()) {
            log.debug("{} method cannot be empty or null", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EXCEPTION);
            log.trace("Leaving");
            return;
        }
        long id = -1;
        try {
            id = Long.parseLong(updateCommand[1]);
        } catch (NumberFormatException e) {
            log.debug("{} the commands second part shoud be interpretable as int", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EXCEPTION);
            log.trace("Leaving");
        }
        String command = updateCommand[2];
        if (command == null || command.isEmpty()) {
            log.debug("{} command cannot be empty or null", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EXCEPTION);
            log.trace("Leaving");
            return;
        }
        // locating account
        for (Map.Entry<Principal, StepUpMethod> entry : stepUpMethodContext.getStepUpMethods().entrySet()) {
            log.debug("Comparing method "+method+" to "+ entry.getValue().getName());
            if (method.equals(entry.getValue().getName())) {
                log.debug("located target method "+method);
                try {
                    for (StepUpAccount account : entry.getValue().getAccounts()) {
                        log.debug("Comparing account id "+id+" to "+account.getId());
                        if (account.getId() == id) {
                            log.debug("located target account "+id);
                            log.debug("running command "+command);
                            accountCommand(command, account);
                        }
                    }
                } catch (Exception e) {
                    log.debug("{} unexpectd exception occurred", getLogPrefix());
                    log.error(e.getMessage());
                    ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EXCEPTION);
                    log.trace("Leaving");
                    return;
                }
            }
        }
        log.debug("Update value to be interpreted is " + updateValue);
        ActionSupport.buildEvent(profileRequestContext, StepUpEventIds.EVENTID_CONTINUE_STEPUP);
        log.trace("Leaving");

    }

    /**
     * dummy version, always disables
     * 
     * @param command
     * @param account
     */
    private void accountCommand(String command, StepUpAccount account) {
        account.setEnabled(false);
    }

}