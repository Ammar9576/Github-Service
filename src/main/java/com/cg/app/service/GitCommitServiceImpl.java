package com.cg.app.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import org.eclipse.egit.github.core.Blob;
import org.eclipse.egit.github.core.Commit;
import org.eclipse.egit.github.core.CommitUser;
import org.eclipse.egit.github.core.Reference;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryBranch;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.Tree;
import org.eclipse.egit.github.core.TreeEntry;
import org.eclipse.egit.github.core.TypedResource;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.DataService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cg.app.builder.EmailBuilder;
import com.cg.app.domain.Email;

@Service
public class GitCommitServiceImpl implements GitCommitService {

	@Autowired
	private MailerService mailerService;

	@Autowired
	private EmailBuilder emailBuilder;

	@Override
	public String doInBackground(GitHubClient client, Repository repository, String branch, String repoName,
			String path, String updatedContent, String commitMessage, String USER) {

		CommitService commitService = null;
		DataService dataService = null;
		TypedResource commitResource = null;

		try {

			RepositoryService repositoryService = new RepositoryService(client);
			commitService = new CommitService(client);
			dataService = new DataService(client);

			String baseCommitSha = null;
			String treeSha = null;
			int count = 0;
			List<RepositoryBranch> repoBranch;
			try {
				repoBranch = repositoryService.getBranches(repository);
				for (RepositoryBranch repositoryBranch : repoBranch) {
					count++;
					if (repositoryBranch.getName().equals(branch)) {
						int index = count - 1;
						baseCommitSha = repositoryService.getBranches(repository).get(index).getCommit().getSha();
						RepositoryCommit baseCommit = commitService.getCommit(repository, baseCommitSha);
						treeSha = baseCommit.getSha();
						break;

					}

				}

			} catch (IOException e1) {
				throw new RuntimeException("Error while Fetching the branches from the Repository");
			}

			Blob blob = new Blob();
			blob.setContent(updatedContent).setEncoding(Blob.ENCODING_UTF8);
			String blob_sha = dataService.createBlob(repository, blob);
			Tree baseTree = dataService.getTree(repository, treeSha);

			// create new tree entry
			TreeEntry treeEntry = new TreeEntry();
			treeEntry.setPath(path);
			treeEntry.setMode(TreeEntry.MODE_BLOB);
			treeEntry.setType(TreeEntry.TYPE_BLOB);
			treeEntry.setSha(blob_sha);
			treeEntry.setSize(blob.getContent().length());

			Collection<TreeEntry> entries = new ArrayList<TreeEntry>();
			entries.add(treeEntry);
			Tree newTree = dataService.createTree(repository, entries, baseTree.getSha());

			Commit commit = new Commit();
			commit.setMessage(commitMessage);
			commit.setTree(newTree);

			UserService userService = new UserService(client);
			CommitUser author = new CommitUser();
			author.setEmail(userService.getEmails().get(0));
			author.setName(USER);
			Calendar now = Calendar.getInstance();
			author.setDate(now.getTime());
			commit.setAuthor(author);
			commit.setCommitter(author);
			List<Commit> listOfCommits = new ArrayList<Commit>();
			listOfCommits.add(new Commit().setSha(baseCommitSha));
			commit.setParents(listOfCommits);

			Commit newCommit = dataService.createCommit(repository, commit);
			// create resource
			commitResource = new TypedResource();
			commitResource.setSha(newCommit.getSha());
			commitResource.setType(TypedResource.TYPE_COMMIT);
			commitResource.setUrl(newCommit.getUrl());

		} catch (Exception e) {
			throw new RuntimeException("Error while Committing the data back to Repository");
		}

		try {
			Reference reference = dataService.getReference(repository, "heads/" + branch);
			reference.setObject(commitResource);
			dataService.editReference(repository, reference, true);

		} catch (IOException e) {

			throw new RuntimeException("Wrong branch data");

		}

		return "Succesfully Updated";
	}

	@Override
	public void sendMail(String appName) {
		Email email = emailBuilder.build(appName);
		try {
			mailerService.mailThroughUitility(email);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
