package io.onedev.server.search.entity.pullrequest;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.OneException;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.User;
import io.onedev.server.model.support.pullrequest.ReviewResult;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;

public class ToBeReviewedByMeCriteria extends EntityCriteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(Root<PullRequest> root, CriteriaBuilder builder) {
		if (User.get() != null) {
			Join<?, ?> join = root.join(PullRequest.PROP_REVIEWS, JoinType.LEFT);
			Path<?> userPath = EntityQuery.getPath(join, PullRequestReview.PROP_USER);
			Path<?> excludeDatePath = EntityQuery.getPath(join, PullRequestReview.PROP_EXCLUDE_DATE);
			Path<?> approvedPath = EntityQuery.getPath(join, PullRequestReview.PROP_RESULT + "." + ReviewResult.PROP_APPROVED);
			join.on(builder.and(
					builder.equal(userPath, User.get()), 
					builder.isNull(excludeDatePath), 
					builder.isNull(approvedPath)));
			return join.isNotNull();
		} else {
			throw new OneException("Please login to perform this query");
		}
	}

	@Override
	public boolean matches(PullRequest request) {
		if (User.get() != null) {
			PullRequestReview review = request.getReview(User.get());
			return review != null 
					&& review.getExcludeDate() == null 
					&& review.getResult() == null;
		} else {
			throw new OneException("Please login to perform this query");
		}
	}

	@Override
	public String toStringWithoutParens() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.ToBeReviewedByMe);
	}

}
