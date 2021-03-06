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

package com.liferay.portlet.expando.model.impl;

import com.liferay.portal.kernel.bean.AutoEscapeBeanHandler;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ProxyUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.model.CacheModel;
import com.liferay.portal.model.impl.BaseModelImpl;

import com.liferay.portlet.expando.model.ExpandoRow;
import com.liferay.portlet.expando.model.ExpandoRowModel;

import java.io.Serializable;

import java.sql.Types;

/**
 * The base model implementation for the ExpandoRow service. Represents a row in the &quot;ExpandoRow&quot; database table, with each column mapped to a property of this class.
 *
 * <p>
 * This implementation and its corresponding interface {@link com.liferay.portlet.expando.model.ExpandoRowModel} exist only as a container for the default property accessors generated by ServiceBuilder. Helper methods and all application logic should be put in {@link ExpandoRowImpl}.
 * </p>
 *
 * @author Brian Wing Shun Chan
 * @see ExpandoRowImpl
 * @see com.liferay.portlet.expando.model.ExpandoRow
 * @see com.liferay.portlet.expando.model.ExpandoRowModel
 * @generated
 */
public class ExpandoRowModelImpl extends BaseModelImpl<ExpandoRow>
	implements ExpandoRowModel {
	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify or reference this class directly. All methods that expect a expando row model instance should use the {@link com.liferay.portlet.expando.model.ExpandoRow} interface instead.
	 */
	public static final String TABLE_NAME = "ExpandoRow";
	public static final Object[][] TABLE_COLUMNS = {
			{ "rowId_", Types.BIGINT },
			{ "companyId", Types.BIGINT },
			{ "tableId", Types.BIGINT },
			{ "classPK", Types.BIGINT }
		};
	public static final String TABLE_SQL_CREATE = "create table ExpandoRow (rowId_ LONG not null primary key,companyId LONG,tableId LONG,classPK LONG)";
	public static final String TABLE_SQL_DROP = "drop table ExpandoRow";
	public static final String DATA_SOURCE = "liferayDataSource";
	public static final String SESSION_FACTORY = "liferaySessionFactory";
	public static final String TX_MANAGER = "liferayTransactionManager";
	public static final boolean ENTITY_CACHE_ENABLED = GetterUtil.getBoolean(com.liferay.portal.util.PropsUtil.get(
				"value.object.entity.cache.enabled.com.liferay.portlet.expando.model.ExpandoRow"),
			true);
	public static final boolean FINDER_CACHE_ENABLED = GetterUtil.getBoolean(com.liferay.portal.util.PropsUtil.get(
				"value.object.finder.cache.enabled.com.liferay.portlet.expando.model.ExpandoRow"),
			true);

	public Class<?> getModelClass() {
		return ExpandoRow.class;
	}

	public String getModelClassName() {
		return ExpandoRow.class.getName();
	}

	public static final long LOCK_EXPIRATION_TIME = GetterUtil.getLong(com.liferay.portal.util.PropsUtil.get(
				"lock.expiration.time.com.liferay.portlet.expando.model.ExpandoRow"));

	public ExpandoRowModelImpl() {
	}

	public long getPrimaryKey() {
		return _rowId;
	}

	public void setPrimaryKey(long primaryKey) {
		setRowId(primaryKey);
	}

	public Serializable getPrimaryKeyObj() {
		return new Long(_rowId);
	}

	public void setPrimaryKeyObj(Serializable primaryKeyObj) {
		setPrimaryKey(((Long)primaryKeyObj).longValue());
	}

	public long getRowId() {
		return _rowId;
	}

	public void setRowId(long rowId) {
		_rowId = rowId;
	}

	public long getCompanyId() {
		return _companyId;
	}

	public void setCompanyId(long companyId) {
		_companyId = companyId;
	}

	public long getTableId() {
		return _tableId;
	}

	public void setTableId(long tableId) {
		if (!_setOriginalTableId) {
			_setOriginalTableId = true;

			_originalTableId = _tableId;
		}

		_tableId = tableId;
	}

	public long getOriginalTableId() {
		return _originalTableId;
	}

	public long getClassPK() {
		return _classPK;
	}

	public void setClassPK(long classPK) {
		if (!_setOriginalClassPK) {
			_setOriginalClassPK = true;

			_originalClassPK = _classPK;
		}

		_classPK = classPK;
	}

	public long getOriginalClassPK() {
		return _originalClassPK;
	}

	@Override
	public ExpandoRow toEscapedModel() {
		if (isEscapedModel()) {
			return (ExpandoRow)this;
		}
		else {
			if (_escapedModelProxy == null) {
				_escapedModelProxy = (ExpandoRow)ProxyUtil.newProxyInstance(_classLoader,
						_escapedModelProxyInterfaces,
						new AutoEscapeBeanHandler(this));
			}

			return _escapedModelProxy;
		}
	}

	@Override
	public Object clone() {
		ExpandoRowImpl expandoRowImpl = new ExpandoRowImpl();

		expandoRowImpl.setRowId(getRowId());
		expandoRowImpl.setCompanyId(getCompanyId());
		expandoRowImpl.setTableId(getTableId());
		expandoRowImpl.setClassPK(getClassPK());

		expandoRowImpl.resetOriginalValues();

		return expandoRowImpl;
	}

	public int compareTo(ExpandoRow expandoRow) {
		long primaryKey = expandoRow.getPrimaryKey();

		if (getPrimaryKey() < primaryKey) {
			return -1;
		}
		else if (getPrimaryKey() > primaryKey) {
			return 1;
		}
		else {
			return 0;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		ExpandoRow expandoRow = null;

		try {
			expandoRow = (ExpandoRow)obj;
		}
		catch (ClassCastException cce) {
			return false;
		}

		long primaryKey = expandoRow.getPrimaryKey();

		if (getPrimaryKey() == primaryKey) {
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return (int)getPrimaryKey();
	}

	@Override
	public void resetOriginalValues() {
		ExpandoRowModelImpl expandoRowModelImpl = this;

		expandoRowModelImpl._originalTableId = expandoRowModelImpl._tableId;

		expandoRowModelImpl._setOriginalTableId = false;

		expandoRowModelImpl._originalClassPK = expandoRowModelImpl._classPK;

		expandoRowModelImpl._setOriginalClassPK = false;
	}

	@Override
	public CacheModel<ExpandoRow> toCacheModel() {
		ExpandoRowCacheModel expandoRowCacheModel = new ExpandoRowCacheModel();

		expandoRowCacheModel.rowId = getRowId();

		expandoRowCacheModel.companyId = getCompanyId();

		expandoRowCacheModel.tableId = getTableId();

		expandoRowCacheModel.classPK = getClassPK();

		return expandoRowCacheModel;
	}

	@Override
	public String toString() {
		StringBundler sb = new StringBundler(9);

		sb.append("{rowId=");
		sb.append(getRowId());
		sb.append(", companyId=");
		sb.append(getCompanyId());
		sb.append(", tableId=");
		sb.append(getTableId());
		sb.append(", classPK=");
		sb.append(getClassPK());
		sb.append("}");

		return sb.toString();
	}

	public String toXmlString() {
		StringBundler sb = new StringBundler(16);

		sb.append("<model><model-name>");
		sb.append("com.liferay.portlet.expando.model.ExpandoRow");
		sb.append("</model-name>");

		sb.append(
			"<column><column-name>rowId</column-name><column-value><![CDATA[");
		sb.append(getRowId());
		sb.append("]]></column-value></column>");
		sb.append(
			"<column><column-name>companyId</column-name><column-value><![CDATA[");
		sb.append(getCompanyId());
		sb.append("]]></column-value></column>");
		sb.append(
			"<column><column-name>tableId</column-name><column-value><![CDATA[");
		sb.append(getTableId());
		sb.append("]]></column-value></column>");
		sb.append(
			"<column><column-name>classPK</column-name><column-value><![CDATA[");
		sb.append(getClassPK());
		sb.append("]]></column-value></column>");

		sb.append("</model>");

		return sb.toString();
	}

	private static ClassLoader _classLoader = ExpandoRow.class.getClassLoader();
	private static Class<?>[] _escapedModelProxyInterfaces = new Class[] {
			ExpandoRow.class
		};
	private long _rowId;
	private long _companyId;
	private long _tableId;
	private long _originalTableId;
	private boolean _setOriginalTableId;
	private long _classPK;
	private long _originalClassPK;
	private boolean _setOriginalClassPK;
	private ExpandoRow _escapedModelProxy;
}