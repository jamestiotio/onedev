package com.gitplex.server.core.gatekeeper;

import org.eclipse.jgit.lib.ObjectId;
import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.gitplex.commons.wicket.editable.annotation.Editable;
import com.gitplex.server.core.GitPlex;
import com.gitplex.server.core.annotation.AccountChoice;
import com.gitplex.server.core.entity.Account;
import com.gitplex.server.core.entity.Depot;
import com.gitplex.server.core.entity.PullRequest;
import com.gitplex.server.core.entity.PullRequestReview;
import com.gitplex.server.core.gatekeeper.checkresult.GateCheckResult;
import com.gitplex.server.core.manager.AccountManager;

@Editable(order=300, icon="fa-user", category=GateKeeper.CATEGORY_USER, description=
		"This gatekeeper will be passed if the commit is disapproved by specified user. "
		+ "It normally works together with a NOT container to reject the pull request "
		+ "in case the user disapproved it")
public class DisapprovedBySpecifiedUser extends AbstractGateKeeper {

	private static final long serialVersionUID = 1L;
	
	private String userName;
	
    @Editable(name="Select User")
    @AccountChoice
    @NotEmpty
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
	
	@Override
	protected GateCheckResult doCheckRequest(PullRequest request) {
		Account user = Preconditions.checkNotNull(GitPlex.getInstance(AccountManager.class).findByName(userName));
		PullRequestReview.Result result = user.checkReviewSince(request.getReferentialUpdate());
		if (result == PullRequestReview.Result.DISAPPROVE) {
            return passed(Lists.newArrayList("Disapproved by " + userName));
		} else {
            return failed(Lists.newArrayList("Not disapproved by " + userName));
		}
	}

	@Override
	protected GateCheckResult doCheckFile(Account user, Depot depot, String branch, String file) {
		return failed(Lists.newArrayList("Not disapproved by " + userName));
	}

	@Override
	protected GateCheckResult doCheckPush(Account user, Depot depot, String refName, 
			ObjectId oldObjectId, ObjectId newObjectId) {
		return failed(Lists.newArrayList("Not disapproved by " + userName));
	}
	
	@Override
	public void onAccountRename(String oldName, String newName) {
		if (userName.equals(oldName))
			userName = newName;
	}

	@Override
	public boolean onAccountDelete(String accountName) {
		return userName.equals(accountName);
	}
	
}
