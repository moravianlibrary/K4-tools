package cz.mzk.k4.tools.domain;

import java.util.Iterator;
import java.util.Set;

/*
 * Process definition
 * https://code.google.com/p/kramerius/wiki/MenuAdministrace#Definice_procesu
 */

/*
 * Example:
 * "uuid":"90b8a07a-14f5-4817-a47f-16f2b88c2724",
 "pid":"20227",
 "def":"mock",
 "state":"FINISHED",
 "batchState":"NO_BATCH",
 "name":"Jmeno procesu mock..",
 "started":"02/07/2012 09:15:31:600",
 "planned":"02/07/2012 09:15:23:570",
 "userid":"krameriusAdmin",
 "userFirstname":"kramerius",
 "userSurname":"admin"
 
 Tabulka "public.processes"
    Sloupec     |             Typ             | Modifikátory
----------------+-----------------------------+--------------
 defid          | character varying(255)      |
 uuid           | character varying(255)      | not null
 pid            | integer                     |
 started        | timestamp without time zone |
 planned        | timestamp without time zone |
 status         | integer                     |
 name           | character varying(1024)     |
 params         | character varying(4096)     |
 startedby      | integer                     |
 token          | character varying(255)      |
 process_id     | integer                     |
 loginname      | character varying(1024)     |
 firstname      | character varying(1024)     |
 surname        | character varying(1024)     |
 user_key       | character varying(255)      |
 params_mapping | character varying(4096)     |
 batch_status   | integer                     |
 finished       | timestamp without time zone |
 token_active   | boolean                     |
 auth_token     | character varying(255)      |
 */

/**
 * 
 * @author Jan Holman
 *
 */
public class KrameriusProcess {

	private String uuid;
	private String pid;
	private String def;
	private String state;
	private String batchState;
	private String name;
	private String started;
	private String planned;
	private String finished;
	private String userid;
	private String userFirstname;
	private String userSurname;
	private Set<KrameriusProcess> children;

	@Override
	public String toString() {
		return "K4Process [uuid=" + uuid + ", pid=" + pid + ", def=" + def
				+ ", state=" + state + ", batchState=" + batchState + ", name="
				+ name + ", started=" + started + ", planned=" + planned
				+ ", userid=" + userid + ", userFirstname=" + userFirstname
				+ ", userSurname=" + userSurname + "]";
	}

	public String toHtml(String tab) {
		String html = "";
		html += "<h4>K4 Process</h4></br>" + tab +
				"<b>uuid:</b> " + getUuid() + "</br>" + tab +
				"<b>pid:</b> " + getPid() + "</br>" + tab +
				"<b>def:</b> " + getDef() + "</br>" + tab +
				"<b>state:</b> " + getState() + "</br>" + tab +
				"<b>batchState:</b> " + getBatchState() + "</br>" + tab +
				"<b>name:</b> " + getName() + "</br>" + tab +
				"<b>planned:</b> " + getPlanned() + "</br>" + tab +
				"<b>started:</b> " + getStarted() + "</br>" + tab +
				"<b>finished:</b> " + getFinished() + "</br>" + tab +
				"<b>userid:</b> " + getUserid() + "</br>" + tab +
				"<b>userFirstname:</b> " + getUserFirstname() + "</br>" + tab +
				"<b>userSurname:</b> " + getUserSurname() + "</br>" + tab;
		if (getChildren() != null) {
			html += "<b>children:</b></br>";
			tab += "	";
			Iterator<KrameriusProcess> iterator = children.iterator();
			KrameriusProcess p = null;
				while (iterator.hasNext()) {
					p = iterator.next();
					html += p.toHtml(tab);
				}
		}
		return html;
	}
	
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public String getDef() {
		return def;
	}

	public void setDef(String def) {
		this.def = def;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getBatchState() {
		return batchState;
	}

	public void setBatchState(String batchState) {
		this.batchState = batchState;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStarted() {
		return started;
	}

	public void setStarted(String started) {
		this.started = started;
	}

	public String getPlanned() {
		return planned;
	}

	public void setPlanned(String planned) {
		this.planned = planned;
	}

	public String getFinished() {
		return finished;
	}

	public void setFinished(String finished) {
		this.finished = finished;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getUserFirstname() {
		return userFirstname;
	}

	public void setUserFirstname(String userFirstname) {
		this.userFirstname = userFirstname;
	}

	public String getUserSurname() {
		return userSurname;
	}

	public void setUserSurname(String userSurname) {
		this.userSurname = userSurname;
	}

	public Set<KrameriusProcess> getChildren() {
		return children;
	}

	public void setChildren(Set<KrameriusProcess> children) {
		this.children = children;
	}
}
