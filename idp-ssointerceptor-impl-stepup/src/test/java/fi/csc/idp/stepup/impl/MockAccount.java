package fi.csc.idp.stepup.impl;

import fi.csc.idp.stepup.api.StepUpAccount;

public class MockAccount implements StepUpAccount {

    @Override
    public long getId() {
        return 0;
    }

    @Override
    public void setId(long id) {
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void setName(String name) {
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public void setEditable(boolean isEditable) {
    }

    @Override
    public void setEnabled(boolean isEnabled) {
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void sendChallenge() throws Exception {
    }

    @Override
    public boolean verifyResponse(String response) throws Exception {
        return "response_success".equals(response);
    }

    @Override
    public void setTarget(String target) {
    }

    @Override
    public String getTarget() {
        return null;
    }

}
