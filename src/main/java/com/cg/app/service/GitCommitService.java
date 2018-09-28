package com.cg.app.service;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;

public interface GitCommitService {
	public String doInBackground(GitHubClient client, Repository repository, String branch, String repoName,
			String path, String updatedContent, String commitMessage, String USER);

	public void sendMail(String appName);
}
