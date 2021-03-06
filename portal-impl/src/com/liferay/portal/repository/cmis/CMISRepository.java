/**
 * Copyright (c) 2000-2011 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.repository.cmis;

import com.liferay.portal.NoSuchRepositoryEntryException;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.repository.RepositoryException;
import com.liferay.portal.kernel.repository.cmis.BaseCmisRepository;
import com.liferay.portal.kernel.repository.cmis.CMISRepositoryHandler;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.FileVersion;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.search.DocumentImpl;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.HitsImpl;
import com.liferay.portal.kernel.search.Query;
import com.liferay.portal.kernel.search.QueryConfig;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.kernel.servlet.PortalSessionThreadLocal;
import com.liferay.portal.kernel.util.AutoResetThreadLocal;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.kernel.util.TransientValue;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.Lock;
import com.liferay.portal.model.RepositoryEntry;
import com.liferay.portal.repository.cmis.model.CMISFileEntry;
import com.liferay.portal.repository.cmis.model.CMISFileVersion;
import com.liferay.portal.repository.cmis.model.CMISFolder;
import com.liferay.portal.repository.cmis.search.CMISQueryBuilder;
import com.liferay.portal.security.auth.PrincipalException;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.persistence.RepositoryEntryUtil;
import com.liferay.portal.util.PropsValues;
import com.liferay.portlet.documentlibrary.DuplicateFileException;
import com.liferay.portlet.documentlibrary.DuplicateFolderNameException;
import com.liferay.portlet.documentlibrary.NoSuchFileEntryException;
import com.liferay.portlet.documentlibrary.NoSuchFileVersionException;
import com.liferay.portlet.documentlibrary.NoSuchFolderException;
import com.liferay.portlet.documentlibrary.model.DLFileEntryConstants;
import com.liferay.portlet.documentlibrary.model.DLFolder;
import com.liferay.portlet.documentlibrary.service.persistence.DLFolderUtil;
import com.liferay.portlet.documentlibrary.util.comparator.RepositoryModelCreateDateComparator;
import com.liferay.portlet.documentlibrary.util.comparator.RepositoryModelModifiedDateComparator;
import com.liferay.portlet.documentlibrary.util.comparator.RepositoryModelNameComparator;
import com.liferay.portlet.documentlibrary.util.comparator.RepositoryModelSizeComparator;

import java.io.InputStream;

import java.math.BigInteger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.Base64;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;

/**
 * CMIS does not provide vendor neutral support for workflow, metadata, tags,
 * categories, etc. They will be ignored in this implementation.
 *
 * @author Alexander Chow
 * @see    <a href="http://wiki.oasis-open.org/cmis/Candidate%20v2%20topics">
 *         Candidate v2 topics</a>
 * @see    <a href="http://wiki.oasis-open.org/cmis/Mixin_Proposal">Mixin /
 *         Aspect Support</a>
 * @see    <a
 *         href="http://www.oasis-open.org/committees/document.php?document_id=39631">
 *         CMIS Type Mutability proposal</a>
 */
public class CMISRepository extends BaseCmisRepository {

	public CMISRepository(CMISRepositoryHandler cmisRepositoryHandler) {
		_cmisRepositoryHandler = cmisRepositoryHandler;
	}

	public FileEntry addFileEntry(
			long folderId, String sourceFileName, String mimeType, String title,
			String description, String changeLog, InputStream is, long size,
			ServiceContext serviceContext)
		throws PortalException, SystemException {

		try {
			Session session = getSession();

			validateTitle(session, folderId, title);

			org.apache.chemistry.opencmis.client.api.Folder cmisFolder =
				getCmisFolder(session, folderId);

			Map<String, Object> properties = new HashMap<String, Object>();

			properties.put(PropertyIds.NAME, title);
			properties.put(
				PropertyIds.OBJECT_TYPE_ID, BaseTypeId.CMIS_DOCUMENT.value());

			ContentStream contentStream = new ContentStreamImpl(
				title, BigInteger.valueOf(size), mimeType, is);

			return toFileEntry(
				cmisFolder.createDocument(properties, contentStream, null));
		}
		catch (PortalException pe) {
			throw pe;
		}
		catch (SystemException se) {
			throw se;
		}
		catch (Exception e) {
			processException(e);

			throw new RepositoryException(e);
		}
	}

	public Folder addFolder(
			long parentFolderId, String title, String description,
			ServiceContext serviceContext)
		throws PortalException, SystemException {

		try {
			Session session = getSession();

			validateTitle(session, parentFolderId, title);

			org.apache.chemistry.opencmis.client.api.Folder cmisFolder =
				getCmisFolder(session, parentFolderId);

			Map<String, Object> properties = new HashMap<String, Object>();

			properties.put(PropertyIds.NAME, title);
			properties.put(
				PropertyIds.OBJECT_TYPE_ID, BaseTypeId.CMIS_FOLDER.value());

			return toFolder(cmisFolder.createFolder(properties));
		}
		catch (PortalException pe) {
			throw pe;
		}
		catch (SystemException se) {
			throw se;
		}
		catch (Exception e) {
			processException(e);

			throw new RepositoryException(e);
		}
	}

	public void cancelCheckOut(long fileEntryId) {
		try {
			Session session = getSession();

			String versionSeriesId = toFileEntryId(fileEntryId);

			Document document = (Document)session.getObject(versionSeriesId);

			document.refresh();

			String versionSeriesCheckedOutId =
				document.getVersionSeriesCheckedOutId();

			if (Validator.isNotNull(versionSeriesCheckedOutId)) {
				document = (Document)session.getObject(
					versionSeriesCheckedOutId);

				document.cancelCheckOut();

				document = (Document)session.getObject(versionSeriesId);

				document.refresh();
			}
		}
		catch (Exception e) {
			_log.error(e, e);
		}
	}

	public void checkInFileEntry(
		long fileEntryId, boolean major, String changeLog,
		ServiceContext serviceContext) {

		try {
			Session session = getSession();

			String versionSeriesId = toFileEntryId(fileEntryId);

			Document document = (Document)session.getObject(versionSeriesId);

			document.refresh();

			String versionSeriesCheckedOutId =
				document.getVersionSeriesCheckedOutId();

			if (Validator.isNotNull(versionSeriesCheckedOutId)) {
				if (!isSupportsMinorVersions()) {
					major = true;
				}

				document = (Document)session.getObject(
					versionSeriesCheckedOutId);

				document.checkIn(major, null, null, changeLog);

				document = (Document)session.getObject(versionSeriesId);

				document.refresh();
			}
		}
		catch (Exception e) {
			_log.error(e, e);
		}
	}

	public void checkInFileEntry(long fileEntryId, String lockUuid) {
		checkInFileEntry(
			fileEntryId, false, StringPool.BLANK, new ServiceContext());
	}

	public FileEntry checkOutFileEntry(long fileEntryId)
		throws PortalException, SystemException {

		try {
			Session session = getSession();

			String versionSeriesId = toFileEntryId(fileEntryId);

			Document document = (Document)session.getObject(versionSeriesId);

			document.refresh();

			document.checkOut();

			document = (Document)session.getObject(versionSeriesId);

			document.refresh();
		}
		catch (Exception e) {
			_log.error(e, e);
		}

		return getFileEntry(fileEntryId);
	}

	public FileEntry checkOutFileEntry(
		long fileEntryId, String owner, long expirationTime) {

		throw new UnsupportedOperationException();
	}

	public FileEntry copyFileEntry(
			long groupId, long fileEntryId, long destFolderId,
			ServiceContext serviceContext)
		throws PortalException, SystemException {

		try {
			Session session = getSession();

			String versionSeriesId = toFileEntryId(fileEntryId);
			String destFolderObjectId = toFolderId(session, destFolderId);

			Document document = (Document)session.getObject(versionSeriesId);

			validateTitle(session, destFolderId, document.getName());

			Document newDocument = document.copy(
				new ObjectIdImpl(destFolderObjectId));

			return toFileEntry(newDocument);
		}
		catch (PortalException pe) {
			throw pe;
		}
		catch (SystemException se) {
			throw se;
		}
		catch (Exception e) {
			processException(e);

			throw new RepositoryException(e);
		}
	}

	public void deleteFileEntry(long fileEntryId)
		throws PortalException, SystemException {

		try {
			Session session = getSession();

			String objectId = toFileEntryId(fileEntryId);

			Document document = (Document)session.getObject(objectId);

			deleteMappedFileEntry(document);

			document.deleteAllVersions();
		}
		catch (PortalException pe) {
			throw pe;
		}
		catch (SystemException se) {
			throw se;
		}
		catch (Exception e) {
			processException(e);

			throw new RepositoryException(e);
		}
	}

	public void deleteFolder(long folderId)
		throws PortalException, SystemException {

		try {
			Session session = getSession();

			org.apache.chemistry.opencmis.client.api.Folder cmisFolder =
				getCmisFolder(session, folderId);

			deleteMappedFolder(cmisFolder);

			cmisFolder.deleteTree(true, UnfileObject.DELETE, false);
		}
		catch (PortalException pe) {
			throw pe;
		}
		catch (SystemException se) {
			throw se;
		}
		catch (Exception e) {
			processException(e);

			throw new RepositoryException(e);
		}
	}

	public List<FileEntry> getFileEntries(
			long folderId, int start, int end, OrderByComparator obc)
		throws SystemException {

		List<FileEntry> fileEntries = getFileEntries(folderId);

		if (obc != null) {
			if (obc instanceof RepositoryModelCreateDateComparator ||
				obc instanceof RepositoryModelModifiedDateComparator ||
				obc instanceof RepositoryModelSizeComparator) {

				fileEntries = ListUtil.sort(fileEntries, obc);
			}
			else if (obc instanceof RepositoryModelNameComparator) {
				if (!obc.isAscending()) {
					fileEntries = ListUtil.sort(fileEntries, obc);
				}
			}
		}

		if ((start == QueryUtil.ALL_POS) && (end == QueryUtil.ALL_POS)) {
			return fileEntries;
		}
		else {
			return ListUtil.subList(fileEntries, start, end);
		}
	}

	public List<FileEntry> getFileEntries(
		long folderId, long fileEntryTypeId, int start, int end,
		OrderByComparator obc) {

		return new ArrayList<FileEntry>();
	}

	public int getFileEntriesCount(long folderId) throws SystemException {
		List<FileEntry> fileEntries = getFileEntries(folderId);

		return fileEntries.size();
	}

	public int getFileEntriesCount(long folderId, long fileEntryTypeId) {
		List<FileEntry> fileEntries = getFileEntries(folderId, fileEntryTypeId);

		return fileEntries.size();
	}

	public FileEntry getFileEntry(long fileEntryId)
		throws PortalException, SystemException {

		try {
			Session session = getSession();

			String objectId = toFileEntryId(fileEntryId);

			Document document = (Document)session.getObject(objectId);

			return toFileEntry(document);
		}
		catch (PortalException pe) {
			throw pe;
		}
		catch (SystemException se) {
			throw se;
		}
		catch (Exception e) {
			processException(e);

			throw new RepositoryException(e);
		}
	}

	public FileEntry getFileEntry(long folderId, String title)
		throws PortalException, SystemException {

		try {
			Session session = getSession();

			String objectId = getObjectId(session, folderId, true, title);

			if (objectId != null) {
				CmisObject cmisObject = session.getObject(objectId);

				Document document = (Document)cmisObject;

				return toFileEntry(document);
			}
		}
		catch (SystemException se) {
			throw se;
		}
		catch (Exception e) {
			processException(e);

			throw new RepositoryException(e);
		}

		throw new NoSuchFileEntryException(
			"No CMIS file entry with {folderId=" + folderId + ", title=" +
				title + "}");
	}

	public FileEntry getFileEntryByUuid(String uuid)
		throws PortalException, SystemException {

		try {
			Session session = getSession();

			RepositoryEntry repositoryEntry = RepositoryEntryUtil.findByUUID_G(
				uuid, getGroupId());

			String objectId = repositoryEntry.getMappedId();

			return toFileEntry((Document)session.getObject(objectId));
		}
		catch (NoSuchRepositoryEntryException nsree) {
			throw new NoSuchFileEntryException(nsree);
		}
		catch (SystemException se) {
			throw se;
		}
		catch (Exception e) {
			processException(e);

			throw new RepositoryException(e);
		}
	}

	public FileVersion getFileVersion(long fileVersionId)
		throws PortalException, SystemException {

		try {
			Session session = getSession();

			return getFileVersion(session, fileVersionId);
		}
		catch (PortalException pe) {
			throw pe;
		}
		catch (SystemException se) {
			throw se;
		}
		catch (Exception e) {
			processException(e);

			throw new RepositoryException(e);
		}
	}

	public Folder getFolder(long folderId)
		throws PortalException, SystemException {

		try {
			Session session = getSession();

			return getFolder(session, folderId);
		}
		catch (PortalException pe) {
			throw pe;
		}
		catch (SystemException se) {
			throw se;
		}
		catch (Exception e) {
			processException(e);

			throw new RepositoryException(e);
		}
	}

	public Folder getFolder(long parentFolderId, String title)
		throws PortalException, SystemException {

		try {
			Session session = getSession();

			String objectId = getObjectId(
				session, parentFolderId, false, title);

			if (objectId != null) {
				CmisObject cmisObject = session.getObject(objectId);

				return toFolder(
					(org.apache.chemistry.opencmis.client.api.Folder)
						cmisObject);
			}
		}
		catch (SystemException se) {
			throw se;
		}
		catch (Exception e) {
			processException(e);

			throw new RepositoryException(e);
		}

		throw new NoSuchFolderException(
			"No CMIS file entry with {parentFolderId=" + parentFolderId +
				", title=" + title + "}");
	}

	public List<Folder> getFolders(
			long parentFolderId, boolean includeMountfolders, int start,
			int end, OrderByComparator obc)
		throws SystemException {

		List<Folder> folders = getFolders(parentFolderId);

		if (obc != null) {
			if (obc instanceof RepositoryModelCreateDateComparator ||
				obc instanceof RepositoryModelModifiedDateComparator) {

				folders = ListUtil.sort(folders, obc);
			}
			else if (obc instanceof RepositoryModelNameComparator) {
				if (!obc.isAscending()) {
					folders = ListUtil.sort(folders, obc);
				}
			}
		}

		if ((start == QueryUtil.ALL_POS) && (end == QueryUtil.ALL_POS)) {
			return folders;
		}
		else {
			return ListUtil.subList(folders, start, end);
		}
	}

	@Override
	public List<Object> getFoldersAndFileEntries(
			long folderId, int start, int end, OrderByComparator obc)
		throws SystemException {

		List<Object> foldersAndFileEntries = getFoldersAndFileEntries(folderId);

		if (obc != null) {
			if (obc instanceof RepositoryModelCreateDateComparator ||
				obc instanceof RepositoryModelModifiedDateComparator ||
				obc instanceof RepositoryModelSizeComparator) {

				foldersAndFileEntries = ListUtil.sort(
					foldersAndFileEntries, obc);
			}
			else if (obc instanceof RepositoryModelNameComparator) {
				if (!obc.isAscending()) {
					foldersAndFileEntries = ListUtil.sort(
						foldersAndFileEntries, obc);
				}
			}
		}

		if ((start == QueryUtil.ALL_POS) && (end == QueryUtil.ALL_POS)) {
			return foldersAndFileEntries;
		}
		else {
			return ListUtil.subList(foldersAndFileEntries, start, end);
		}
	}

	@Override
	public int getFoldersAndFileEntriesCount(long folderId)
		throws SystemException {

		List<Object> foldersAndFileEntries = getFoldersAndFileEntries(folderId);

		return foldersAndFileEntries.size();
	}

	public int getFoldersCount(long parentFolderId, boolean includeMountfolders)
		throws SystemException {

		List<Folder> folders = getFolders(parentFolderId);

		return folders.size();
	}

	public int getFoldersFileEntriesCount(List<Long> folderIds, int status)
		throws SystemException {

		int count = 0;

		for (long folderId : folderIds) {
			List<FileEntry> fileEntries = getFileEntries(folderId);

			count += fileEntries.size();
		}

		return count;
	}

	@Override
	public String getLatestVersionId(String objectId) throws SystemException {
		try {
			Session session = getSession();

			Document document = (Document)session.getObject(objectId);

			List<Document> documentVersions = document.getAllVersions();

			document = documentVersions.get(0);

			return document.getId();
		}
		catch (Exception e) {
			throw new RepositoryException(e);
		}
	}

	public List<Folder> getMountFolders(
		long parentFolderId, int start, int end, OrderByComparator obc) {

		return new ArrayList<Folder>();
	}

	public int getMountFoldersCount(long parentFolderId) {
		return 0;
	}

	@Override
	public String getObjectName(String objectId)
		throws PortalException, SystemException {

		Session session = getSession();

		CmisObject cmisObject = session.getObject(objectId);

		return cmisObject.getName();
	}

	@Override
	public List<String> getObjectPaths(String objectId)
		throws PortalException, SystemException {

		Session session = getSession();

		CmisObject cmisObject = session.getObject(objectId);

		if (cmisObject instanceof FileableCmisObject) {
			FileableCmisObject fileableCmisObject =
				(FileableCmisObject)cmisObject;

			return fileableCmisObject.getPaths();
		}

		throw new RepositoryException(
			"CMIS object is unfileable for id " + objectId);
	}

	public Session getSession() throws PortalException, RepositoryException {
		Session session = getCachedSession();

		if (session != null) {
			return session;
		}

		SessionImpl sessionImpl =
			(SessionImpl)_cmisRepositoryHandler.getSession();

		session = sessionImpl.getSession();

		setCachedSession(session);

		return session;
	}

	public void getSubfolderIds(List<Long> folderIds, long folderId)
		throws SystemException {

		try {
			List<Folder> subfolders = getFolders(
				folderId, false, QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);

			getSubfolderIds(folderIds, subfolders, true);
		}
		catch (SystemException se) {
			throw se;
		}
		catch (Exception e) {
			throw new RepositoryException(e);
		}
	}

	public List<Long> getSubfolderIds(long folderId, boolean recurse)
		throws SystemException {

		try {
			List<Long> subfolderIds = new ArrayList<Long>();

			List<Folder> subfolders = getFolders(
				folderId, false, QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);

			getSubfolderIds(subfolderIds, subfolders, recurse);

			return subfolderIds;
		}
		catch (SystemException se) {
			throw se;
		}
		catch (Exception e) {
			throw new RepositoryException(e);
		}
	}

	public String[] getSupportedConfigurations() {
		return _cmisRepositoryHandler.getSupportedConfigurations();
	}

	public String[][] getSupportedParameters() {
		return _cmisRepositoryHandler.getSupportedParameters();
	}

	@Override
	public void initRepository() throws PortalException, SystemException {
		try {
			_sessionKey =
				Session.class.getName().concat(StringPool.POUND).concat(
					String.valueOf(getRepositoryId()));

			Session session = getSession();

			session.getRepositoryInfo();
		}
		catch (PortalException pe) {
			throw pe;
		}
		catch (SystemException se) {
			throw se;
		}
		catch (Exception e) {
			processException(e);

			throw new RepositoryException(
				"Unable to initialize CMIS session for repository " +
					getRepositoryId(), e);
		}
	}

	@Override
	public boolean isCancelCheckOutAllowable(String objectId)
		throws PortalException, SystemException {

		return isActionAllowable(objectId, Action.CAN_CANCEL_CHECK_OUT);
	}

	@Override
	public boolean isCheckInAllowable(String objectId)
		throws PortalException, SystemException {

		return isActionAllowable(objectId, Action.CAN_CHECK_IN);
	}

	@Override
	public boolean isCheckOutAllowable(String objectId)
		throws PortalException, SystemException {

		return isActionAllowable(objectId, Action.CAN_CHECK_OUT);
	}

	public boolean isDocumentRetrievableByVersionSeriesId() {
		return _cmisRepositoryHandler.isDocumentRetrievableByVersionSeriesId();
	}

	public boolean isRefreshBeforePermissionCheck() {
		return _cmisRepositoryHandler.isRefreshBeforePermissionCheck();
	}

	@Override
	public boolean isSupportsMinorVersions()
		throws PortalException, SystemException {

		try {
			Session session = getSession();

			RepositoryInfo repositoryInfo = session.getRepositoryInfo();

			String productName = repositoryInfo.getProductName();

			return _cmisRepositoryHandler.isSupportsMinorVersions(productName);
		}
		catch (PortalException pe) {
			throw pe;
		}
		catch (SystemException se) {
			throw se;
		}
		catch (Exception e) {
			processException(e);

			throw new RepositoryException(e);
		}
	}

	public Lock lockFolder(long folderId) {
		throw new UnsupportedOperationException();
	}

	public Lock lockFolder(
		long folderId, String owner, boolean inheritable, long expirationTime) {

		throw new UnsupportedOperationException();
	}

	public FileEntry moveFileEntry(
			long fileEntryId, long newFolderId, ServiceContext serviceContext)
		throws PortalException, SystemException {

		try {
			Session session = getSession();

			String versionSeriesId = toFileEntryId(fileEntryId);
			String newFolderObjectId = toFolderId(session, newFolderId);

			Document document = (Document)session.getObject(versionSeriesId);

			validateTitle(session, newFolderId, document.getName());

			String oldFolderObjectId = document.getParents().get(0).getId();

			if (oldFolderObjectId.equals(newFolderObjectId)) {
				return toFileEntry(document);
			}

			document = (Document)document.move(
				new ObjectIdImpl(oldFolderObjectId),
				new ObjectIdImpl(newFolderObjectId));

			String newObjectId = document.getVersionSeriesId();

			if (!versionSeriesId.equals(newObjectId)) {
				document = (Document)session.getObject(newObjectId);

				updateMappedId(fileEntryId, document.getVersionSeriesId());
			}

			FileEntry fileEntry = toFileEntry(document);

			document = null;

			return fileEntry;
		}
		catch (PortalException pe) {
			throw pe;
		}
		catch (SystemException se) {
			throw se;
		}
		catch (Exception e) {
			processException(e);

			throw new RepositoryException(e);
		}
	}

	public Folder moveFolder(
			long folderId, long parentFolderId, ServiceContext serviceContext)
		throws PortalException, SystemException {

		try {
			Session session = getSession();

			String objectId = toFolderId(session, folderId);

			org.apache.chemistry.opencmis.client.api.Folder cmisFolder =
				(org.apache.chemistry.opencmis.client.api.Folder)
					session.getObject(objectId);

			validateTitle(session, parentFolderId, cmisFolder.getName());

			org.apache.chemistry.opencmis.client.api.Folder parentCmisFolder =
				cmisFolder.getFolderParent();

			if (parentCmisFolder == null) {
				throw new RepositoryException(
					"Cannot move CMIS root folder " + folderId);
			}

			String sourceFolderId = parentCmisFolder.getId();

			String targetFolderId = toFolderId(session, parentFolderId);

			if (!sourceFolderId.equals(targetFolderId) &&
				!targetFolderId.equals(objectId)) {

				cmisFolder =
					(org.apache.chemistry.opencmis.client.api.Folder)
						cmisFolder.move(
							new ObjectIdImpl(sourceFolderId),
							new ObjectIdImpl(targetFolderId));
			}

			return toFolder(cmisFolder);
		}
		catch (PortalException pe) {
			throw pe;
		}
		catch (SystemException se) {
			throw se;
		}
		catch (Exception e) {
			processException(e);

			throw new RepositoryException(e);
		}
	}

	public Lock refreshFileEntryLock(String lockUuid, long expirationTime) {
		throw new UnsupportedOperationException();
	}

	public Lock refreshFolderLock(String lockUuid, long expirationTime) {
		throw new UnsupportedOperationException();
	}

	public void revertFileEntry(
			long fileEntryId, String version, ServiceContext serviceContext)
		throws PortalException, SystemException {

		try {
			Session session = getSession();

			String versionSeriesId = toFileEntryId(fileEntryId);

			Document document = (Document)session.getObject(versionSeriesId);

			Document oldVersion = null;

			List<Document> documentVersions = document.getAllVersions();

			for (Document currentVersion : documentVersions) {
				String currentVersionLabel = currentVersion.getVersionLabel();

				if (Validator.isNull(currentVersionLabel)) {
					currentVersionLabel = DLFileEntryConstants.VERSION_DEFAULT;
				}

				if (currentVersionLabel.equals(version)) {
					oldVersion = currentVersion;

					break;
				}
			}

			String mimeType = oldVersion.getContentStreamMimeType();
			String changeLog = "Reverted to " + version;
			String title = oldVersion.getName();
			ContentStream contentStream = oldVersion.getContentStream();

			updateFileEntry(
				fileEntryId, contentStream.getFileName(), mimeType, title,
				StringPool.BLANK, changeLog, true, contentStream.getStream(),
				contentStream.getLength(), serviceContext);
		}
		catch (PortalException pe) {
			throw pe;
		}
		catch (SystemException se) {
			throw se;
		}
		catch (Exception e) {
			processException(e);

			throw new RepositoryException(e);
		}
	}

	public Hits search(SearchContext searchContext, Query query)
		throws SearchException {

		try {
			return doSearch(searchContext, query);
		}
		catch (Exception e) {
			throw new SearchException(e);
		}
	}

	public FileEntry toFileEntry(Document document) throws SystemException {
		Object[] ids = null;

		if (isDocumentRetrievableByVersionSeriesId()) {
			ids = getRepositoryEntryIds(document.getVersionSeriesId());
		}
		else {
			ids = getRepositoryEntryIds(document.getId());
		}

		long fileEntryId = (Long)ids[0];
		String uuid = (String)ids[1];

		return new CMISFileEntry(this, uuid, fileEntryId, document);
	}

	@Override
	public FileEntry toFileEntry(String objectId)
		throws PortalException, SystemException {

		Session session = getSession();

		Document document = (Document)session.getObject(objectId);

		return toFileEntry(document);
	}

	public FileVersion toFileVersion(Document version) throws SystemException {
		Object[] ids = getRepositoryEntryIds(version.getId());

		long fileVersionId = (Long)ids[0];

		return new CMISFileVersion(this, fileVersionId, version);
	}

	public Folder toFolder(
			org.apache.chemistry.opencmis.client.api.Folder cmisFolder)
		throws SystemException {

		Object[] ids = getRepositoryEntryIds(cmisFolder.getId());

		long folderId = (Long)ids[0];
		String uuid = (String)ids[1];

		return new CMISFolder(this, uuid, folderId, cmisFolder);
	}

	@Override
	public Folder toFolder(String objectId)
		throws PortalException, SystemException {

		Session session = getSession();

		org.apache.chemistry.opencmis.client.api.Folder cmisFolder =
			(org.apache.chemistry.opencmis.client.api.Folder)session.getObject(
				objectId);

		return toFolder(cmisFolder);
	}

	public void unlockFolder(long folderId, String lockUuid) {
		throw new UnsupportedOperationException();
	}

	public FileEntry updateFileEntry(
			long fileEntryId, String sourceFileName, String mimeType,
			String title, String description, String changeLog,
			boolean majorVersion, InputStream is, long size,
			ServiceContext serviceContext)
		throws PortalException, SystemException {

		Document document = null;

		ObjectId checkOutDocumentObjectId = null;

		try {
			Session session = getSession();

			String versionSeriesId = toFileEntryId(fileEntryId);

			document = (Document)session.getObject(versionSeriesId);

			String versionSeriesCheckedOutId =
				document.getVersionSeriesCheckedOutId();

			if (Validator.isNotNull(versionSeriesCheckedOutId)) {
				document = (Document)session.getObject(
					versionSeriesCheckedOutId);

				document.refresh();
			}

			String currentTitle = document.getName();

			AllowableActions allowableActions = document.getAllowableActions();

			Set<Action> allowableActionsSet =
				allowableActions.getAllowableActions();

			if (allowableActionsSet.contains(Action.CAN_CHECK_OUT)) {
				checkOutDocumentObjectId = document.checkOut();

				document = (Document)session.getObject(
					checkOutDocumentObjectId);
			}

			Map<String, Object> properties = null;

			ContentStream contentStream = null;

			if (Validator.isNotNull(title) && !title.equals(currentTitle)) {
				properties = new HashMap<String, Object>();

				properties.put(PropertyIds.NAME, title);
			}

			if (is != null) {
				contentStream = new ContentStreamImpl(
					sourceFileName, BigInteger.valueOf(size), mimeType, is);
			}

			checkUpdatable(allowableActionsSet, properties, contentStream);

			if (checkOutDocumentObjectId != null) {
				if (!isSupportsMinorVersions()) {
					majorVersion = true;
				}

				document.checkIn(
					majorVersion, properties, contentStream, changeLog);

				checkOutDocumentObjectId = null;
			}
			else {
				if (properties != null) {
					document = (Document)document.updateProperties(properties);
				}

				if (contentStream != null) {
					document.setContentStream(contentStream, true, false);
				}
			}

			document = (Document)session.getObject(versionSeriesId);

			return toFileEntry(document);
		}
		catch (PortalException pe) {
			throw pe;
		}
		catch (SystemException se) {
			throw se;
		}
		catch (Exception e) {
			processException(e);

			throw new RepositoryException(e);
		}
		finally {
			if (checkOutDocumentObjectId != null) {
				document.cancelCheckOut();
			}
		}
	}

	@Override
	public FileEntry updateFileEntry(
			String objectId, String mimeType, Map<String, Object> properties,
			InputStream is, String sourceFileName, long size,
			ServiceContext serviceContext)
		throws PortalException, SystemException {

		try {
			Session session = getSession();

			Document document = (Document)session.getObject(objectId);

			AllowableActions allowableActions = document.getAllowableActions();

			Set<Action> allowableActionsSet =
				allowableActions.getAllowableActions();

			ContentStream contentStream = null;

			if (is != null) {
				is = new Base64.InputStream(is, Base64.ENCODE);

				contentStream = new ContentStreamImpl(
					sourceFileName, BigInteger.valueOf(size), mimeType, is);
			}

			checkUpdatable(allowableActionsSet, properties, contentStream);

			if (properties != null) {
				document = (Document)document.updateProperties(properties);
			}

			if (contentStream != null) {
				document.setContentStream(contentStream, true, false);
			}

			return toFileEntry(document);
		}
		catch (PortalException pe) {
			throw pe;
		}
		catch (SystemException se) {
			throw se;
		}
		catch (Exception e) {
			processException(e);

			throw new RepositoryException(e);
		}
	}

	public Folder updateFolder(
			long folderId, String title, String description,
			ServiceContext serviceContext)
		throws PortalException, SystemException {

		try {
			Session session = getSession();

			String objectId = toFolderId(session, folderId);

			org.apache.chemistry.opencmis.client.api.Folder cmisFolder =
				(org.apache.chemistry.opencmis.client.api.Folder)
					session.getObject(objectId);

			String currentTitle = cmisFolder.getName();

			Map<String, Object> properties = new HashMap<String, Object>();

			if (Validator.isNotNull(title) && !title.equals(currentTitle)) {
				properties.put(PropertyIds.NAME, title);
			}

			String newObjectId = cmisFolder.updateProperties(
				properties, true).getId();

			if (!objectId.equals(newObjectId)) {
				cmisFolder =
					(org.apache.chemistry.opencmis.client.api.Folder)
						session.getObject(newObjectId);

				updateMappedId(folderId, newObjectId);
			}

			return toFolder(cmisFolder);
		}
		catch (PortalException pe) {
			throw pe;
		}
		catch (SystemException se) {
			throw se;
		}
		catch (Exception e) {
			processException(e);

			throw new RepositoryException(e);
		}
	}

	public boolean verifyFileEntryCheckOut(long fileEntryId, String lockUuid) {
		throw new UnsupportedOperationException();
	}

	public boolean verifyInheritableLock(long folderId, String lockUuid) {
		throw new UnsupportedOperationException();
	}

	protected void cacheFoldersAndFileEntries(long folderId)
		throws SystemException {

		try {
			Map<Long, List<Object>> foldersAndFileEntriesCache =
				_foldersAndFileEntriesCache.get();

			if (foldersAndFileEntriesCache.containsKey(folderId)) {
				return;
			}

			List<Object> foldersAndFileEntries = new ArrayList<Object>();
			List<Folder> folders = new ArrayList<Folder>();
			List<FileEntry> fileEntries = new ArrayList<FileEntry>();

			Session session = getSession();

			org.apache.chemistry.opencmis.client.api.Folder cmisParentFolder =
				getCmisFolder(session, folderId);

			Folder parentFolder = toFolder(cmisParentFolder);

			ItemIterable<CmisObject> cmisObjects =
				cmisParentFolder.getChildren();

			Iterator<CmisObject> itr = cmisObjects.iterator();

			while (itr.hasNext()) {
				CmisObject cmisObject = itr.next();

				if (cmisObject instanceof
						org.apache.chemistry.opencmis.client.api.Folder) {

					CMISFolder cmisFolder = (CMISFolder)toFolder(
						(org.apache.chemistry.opencmis.client.api.Folder)
							cmisObject);

					cmisFolder.setParentFolder(parentFolder);

					foldersAndFileEntries.add(cmisFolder);
					folders.add(cmisFolder);
				}
				else if (cmisObject instanceof Document) {
					CMISFileEntry cmisFileEntry = (CMISFileEntry)toFileEntry(
						(Document)cmisObject);

					cmisFileEntry.setParentFolder(parentFolder);

					foldersAndFileEntries.add(cmisFileEntry);
					fileEntries.add(cmisFileEntry);
				}
			}

			foldersAndFileEntriesCache.put(folderId, foldersAndFileEntries);

			Map<Long, List<Folder>> foldersCache = _foldersCache.get();

			foldersCache.put(folderId, folders);

			Map<Long, List<FileEntry>> fileEntriesCache =
				_fileEntriesCache.get();

			fileEntriesCache.put(folderId, fileEntries);
		}
		catch (SystemException se) {
			throw se;
		}
		catch (Exception e) {
			throw new RepositoryException(e);
		}
	}

	protected void checkUpdatable(
			Set<Action> allowableActionsSet, Map<String, Object> properties,
			ContentStream contentStream)
		throws PrincipalException {

		if (properties != null) {
			if (!allowableActionsSet.contains(Action.CAN_UPDATE_PROPERTIES)) {
				throw new PrincipalException();
			}
		}

		if (contentStream != null) {
			if (!allowableActionsSet.contains(Action.CAN_SET_CONTENT_STREAM)) {
				throw new PrincipalException();
			}
		}
	}

	protected void deleteMappedFileEntry(Document document)
		throws SystemException {

		if (PropsValues.DL_REPOSITORY_CMIS_DELETE_DEPTH == _DELETE_NONE) {
			return;
		}

		List<Document> documentVersions = document.getAllVersions();

		for (Document version : documentVersions) {
			try {
				RepositoryEntryUtil.removeByR_M(
					getRepositoryId(), version.getId());
			}
			catch (NoSuchRepositoryEntryException nsree) {
			}
		}

		try {
			RepositoryEntryUtil.removeByR_M(
				getRepositoryId(), document.getId());
		}
		catch (NoSuchRepositoryEntryException nsree) {
		}
	}

	protected void deleteMappedFolder(
			org.apache.chemistry.opencmis.client.api.Folder cmisFolder)
		throws SystemException {

		if (PropsValues.DL_REPOSITORY_CMIS_DELETE_DEPTH == _DELETE_NONE) {
			return;
		}

		ItemIterable<CmisObject> cmisObjects = cmisFolder.getChildren();

		Iterator<CmisObject> itr = cmisObjects.iterator();

		while (itr.hasNext()) {
			CmisObject cmisObject = itr.next();

			if (cmisObject instanceof Document) {
				Document document = (Document)cmisObject;

				deleteMappedFileEntry(document);
			}
			else if (cmisObject instanceof
						org.apache.chemistry.opencmis.client.api.Folder) {

				org.apache.chemistry.opencmis.client.api.Folder cmisSubfolder =
					(org.apache.chemistry.opencmis.client.api.Folder)cmisObject;

				try {
					RepositoryEntryUtil.removeByR_M(
						getRepositoryId(), cmisObject.getId());

					if (PropsValues.DL_REPOSITORY_CMIS_DELETE_DEPTH ==
							_DELETE_DEEP) {

						deleteMappedFolder(cmisSubfolder);
					}
				}
				catch (NoSuchRepositoryEntryException nsree) {
				}
			}
		}
	}

	protected Hits doSearch(SearchContext searchContext, Query query)
		throws Exception {

		long startTime = System.currentTimeMillis();

		Session session = getSession();

		String queryString = CMISQueryBuilder.buildQuery(searchContext, query);

		ItemIterable<QueryResult> queryResults = session.query(
			queryString, false);

		long total = queryResults.getTotalNumItems();

		int start = searchContext.getStart();
		int end = searchContext.getEnd();

		if ((start == QueryUtil.ALL_POS) && (end == QueryUtil.ALL_POS)) {
			start = 0;
			end = (int)total;
		}

		int subsetTotal = end - start;

		com.liferay.portal.kernel.search.Document[] documents =
			new DocumentImpl[subsetTotal];
		String[] snippets = new String[subsetTotal];
		float[] scores = new float[subsetTotal];

		QueryConfig queryConfig = query.getQueryConfig();

		int index = 0;

		queryResults = queryResults.skipTo(start);

		Iterator<QueryResult> itr = queryResults.iterator();

		for (int i = start; i < end; i++) {
			QueryResult queryResult = itr.next();

			com.liferay.portal.kernel.search.Document document =
				new DocumentImpl();

			String objectId = queryResult.getPropertyValueByQueryName(
				PropertyIds.OBJECT_ID);

			FileEntry fileEntry = toFileEntry(objectId);

			document.addKeyword(
				Field.ENTRY_CLASS_NAME, fileEntry.getModelClassName());
			document.addKeyword(
				Field.ENTRY_CLASS_PK, fileEntry.getFileEntryId());
			document.addKeyword(Field.TITLE, fileEntry.getTitle());

			documents[index] = document;

			if (queryConfig.isScoreEnabled()) {
				scores[index] = (Float)queryResult.getPropertyValueByQueryName(
					"SCORE");
			}
			else {
				scores[index] = 1;
			}

			snippets[index] = StringPool.BLANK;

			index++;
		}

		float searchTime =
			(float)(System.currentTimeMillis() - startTime) / Time.SECOND;

		Hits hits = new HitsImpl();

		hits.setDocs(documents);
		hits.setLength((int)total);
		hits.setQuery(query);
		hits.setQueryTerms(new String[0]);
		hits.setScores(scores);
		hits.setSearchTime(searchTime);
		hits.setSnippets(snippets);
		hits.setStart(startTime);

		return hits;
	}

	protected Session getCachedSession() {
		HttpSession httpSession = PortalSessionThreadLocal.getHttpSession();

		if (httpSession == null) {
			return null;
		}

		TransientValue<Session> transientValue =
			(TransientValue<Session>)httpSession.getAttribute(_sessionKey);

		if (transientValue == null) {
			return null;
		}

		return transientValue.getValue();
	}

	protected org.apache.chemistry.opencmis.client.api.Folder getCmisFolder(
			Session session, long folderId)
		throws PortalException, SystemException {

		Folder folder = getFolder(session, folderId);

		org.apache.chemistry.opencmis.client.api.Folder cmisFolder =
			(org.apache.chemistry.opencmis.client.api.Folder)folder.getModel();

		return cmisFolder;
	}

	protected List<FileEntry> getFileEntries(long folderId)
		throws SystemException {

		cacheFoldersAndFileEntries(folderId);

		Map<Long, List<FileEntry>> fileEntriesCache =
			_fileEntriesCache.get();

		return fileEntriesCache.get(folderId);
	}

	protected List<FileEntry> getFileEntries(long folderId, long repositoryId) {
		return new ArrayList<FileEntry>();
	}

	protected FileVersion getFileVersion(Session session, long fileVersionId)
		throws PortalException, SystemException {

		String objectId = toFileVersionId(fileVersionId);

		return toFileVersion((Document)session.getObject(objectId));
	}

	protected Folder getFolder(Session session, long folderId)
		throws PortalException, SystemException {

		String objectId = toFolderId(session, folderId);

		CmisObject cmisObject = session.getObject(objectId);

		return (Folder)toFolderOrFileEntry(cmisObject);
	}

	protected List<Folder> getFolders(long folderId) throws SystemException {
		cacheFoldersAndFileEntries(folderId);

		Map<Long, List<Folder>> foldersCache = _foldersCache.get();

		return foldersCache.get(folderId);
	}

	protected List<Object> getFoldersAndFileEntries(long folderId)
		throws SystemException {

		cacheFoldersAndFileEntries(folderId);

		Map<Long, List<Object>> foldersAndFileEntriesCache =
			_foldersAndFileEntriesCache.get();

		return foldersAndFileEntriesCache.get(folderId);
	}

	protected String getObjectId(
			Session session, long folderId, boolean fileEntry, String name)
		throws SystemException {

		try {
			String objectId = toFolderId(session, folderId);

			StringBundler sb = new StringBundler(7);

			sb.append("SELECT cmis:objectId FROM ");

			if (fileEntry) {
				sb.append("cmis:document ");
			}
			else {
				sb.append("cmis:folder ");
			}

			sb.append("WHERE cmis:name = '");
			sb.append(name);
			sb.append("' AND IN_FOLDER('");
			sb.append(objectId);
			sb.append("')");

			String query = sb.toString();

			if (_log.isDebugEnabled()) {
				_log.debug("Calling query " + query);
			}

			ItemIterable<QueryResult> queryResults = session.query(
				query, false);

			Iterator<QueryResult> itr = queryResults.iterator();

			if (itr.hasNext()) {
				QueryResult queryResult = itr.next();

				PropertyData<String> propertyData = queryResult.getPropertyById(
					PropertyIds.OBJECT_ID);

				List<String> values = propertyData.getValues();

				return values.get(0);
			}

			return null;
		}
		catch (SystemException se) {
			throw se;
		}
		catch (Exception e) {
			throw new RepositoryException(e);
		}
	}

	protected void getSubfolderIds(
			List<Long> subfolderIds, List<Folder> subfolders, boolean recurse)
		throws SystemException {

		for (Folder subfolder : subfolders) {
			long subfolderId = subfolder.getFolderId();

			subfolderIds.add(subfolderId);

			if (recurse) {
				List<Folder> subSubFolders = getFolders(
					subfolderId, false, QueryUtil.ALL_POS, QueryUtil.ALL_POS,
					null);

				getSubfolderIds(subfolderIds, subSubFolders, recurse);
			}
		}
	}

	protected boolean isActionAllowable(String objectId, Action action)
		throws PortalException, SystemException {

		Session session = getSession();

		Document document = (Document)session.getObject(objectId);

		AllowableActions allowableActions = document.getAllowableActions();

		Set<Action> allowableActionsSet =
			allowableActions.getAllowableActions();

		if (allowableActionsSet.contains(action)) {
			return true;
		}
		else {
			return false;
		}
	}

	protected void processException(Exception e) throws PortalException {
		if ((e instanceof CmisRuntimeException &&
			 e.getMessage().contains("authorized")) ||
			(e instanceof CmisPermissionDeniedException)) {

			String message = e.getMessage();

			try {
				message =
					"Unable to login with user " +
						_cmisRepositoryHandler.getLogin();
			}
			catch (Exception e2) {
			}

			throw new PrincipalException(message, e);
		}
	}

	protected void setCachedSession(Session session) {
		HttpSession httpSession = PortalSessionThreadLocal.getHttpSession();

		if (httpSession == null) {
			if (_log.isWarnEnabled()) {
				_log.warn("Unable to get HTTP session");
			}

			return;
		}

		httpSession.setAttribute(
			_sessionKey, new TransientValue<Session>(session));
	}

	protected String toFileEntryId(long fileEntryId)
		throws PortalException, SystemException {

		RepositoryEntry repositoryEntry = RepositoryEntryUtil.fetchByPrimaryKey(
			fileEntryId);

		if (repositoryEntry == null) {
			throw new NoSuchFileEntryException(
				"No CMIS file entry with {fileEntryId=" + fileEntryId + "}");
		}

		return repositoryEntry.getMappedId();
	}

	protected String toFileVersionId(long fileVersionId)
		throws PortalException, SystemException {

		RepositoryEntry repositoryEntry = RepositoryEntryUtil.fetchByPrimaryKey(
			fileVersionId);

		if (repositoryEntry == null) {
			throw new NoSuchFileVersionException(
				"No CMIS file version with {fileVersionId=" + fileVersionId +
					"}");
		}

		return repositoryEntry.getMappedId();
	}

	protected String toFolderId(Session session, long folderId)
		throws PortalException, SystemException {

		RepositoryEntry repositoryEntry =
			RepositoryEntryUtil.fetchByPrimaryKey(folderId);

		if (repositoryEntry != null) {
			return repositoryEntry.getMappedId();
		}

		DLFolder dlFolder = DLFolderUtil.fetchByPrimaryKey(folderId);

		if (dlFolder == null) {
			throw new NoSuchFolderException(
				"No CMIS folder with {folderId=" + folderId + "}");
		}
		else if (!dlFolder.isMountPoint()) {
			throw new RepositoryException(
				"CMIS repository should not be used for folder ID " + folderId);
		}

		RepositoryInfo repositoryInfo = session.getRepositoryInfo();

		String rootFolderId = repositoryInfo.getRootFolderId();

		repositoryEntry = RepositoryEntryUtil.fetchByR_M(
			getRepositoryId(), rootFolderId);

		if (repositoryEntry == null) {
			long repositoryEntryId = counterLocalService.increment();

			repositoryEntry = RepositoryEntryUtil.create(repositoryEntryId);

			repositoryEntry.setGroupId(getGroupId());
			repositoryEntry.setRepositoryId(getRepositoryId());
			repositoryEntry.setMappedId(rootFolderId);

			RepositoryEntryUtil.update(repositoryEntry, false);
		}

		return repositoryEntry.getMappedId();
	}

	protected Object toFolderOrFileEntry(CmisObject cmisObject)
		throws SystemException {

		if (cmisObject instanceof Document) {
			FileEntry fileEntry = toFileEntry((Document)cmisObject);

			return fileEntry;
		}
		else if (cmisObject instanceof
					org.apache.chemistry.opencmis.client.api.Folder) {

			org.apache.chemistry.opencmis.client.api.Folder cmisFolder =
				(org.apache.chemistry.opencmis.client.api.Folder)cmisObject;

			Folder folder = toFolder(cmisFolder);

			return folder;
		}
		else {
			return null;
		}
	}

	protected void updateMappedId(long repositoryEntryId, String mappedId)
		throws NoSuchRepositoryEntryException, SystemException {

		RepositoryEntry repositoryEntry = RepositoryEntryUtil.findByPrimaryKey(
			repositoryEntryId);

		if (!mappedId.equals(repositoryEntry.getMappedId())) {
			repositoryEntry.setMappedId(mappedId);

			RepositoryEntryUtil.update(repositoryEntry, false);
		}
	}

	protected void validateTitle(Session session, long folderId, String title)
		throws PortalException, SystemException {

		String objectId = getObjectId(session, folderId, true, title);

		if (objectId != null) {
			throw new DuplicateFileException(title);
		}

		objectId = getObjectId(session, folderId, false, title);

		if (objectId != null) {
			throw new DuplicateFolderNameException(title);
		}
	}

	private static final int _DELETE_DEEP = -1;

	private static final int _DELETE_NONE = 0;

	private static Log _log = LogFactoryUtil.getLog(CMISRepository.class);

	private static ThreadLocal<Map<Long, List<FileEntry>>> _fileEntriesCache =
		new AutoResetThreadLocal<Map<Long, List<FileEntry>>>(
			CMISRepository.class + "._fileEntriesCache",
			new HashMap<Long, List<FileEntry>>());
	private static ThreadLocal<Map<Long, List<Object>>>
		_foldersAndFileEntriesCache =
			new AutoResetThreadLocal<Map<Long, List<Object>>>(
				CMISRepository.class + "._foldersAndFileEntriesCache",
				new HashMap<Long, List<Object>>());
	private static ThreadLocal<Map<Long, List<Folder>>> _foldersCache =
		new AutoResetThreadLocal<Map<Long, List<Folder>>>(
			CMISRepository.class + "._foldersCache",
			new HashMap<Long, List<Folder>>());

	private CMISRepositoryHandler _cmisRepositoryHandler;
	private String _sessionKey;

}