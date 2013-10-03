package cz.mzk.k4.tools.domain;

/**
 * 
 * @author Jan Holman
 *
 */
public class ProcessLog {

	private String sout;
	private String serr;

	public String getSout() {
		return sout;
	}

	public void setSout(String sout) {
		this.sout = sout;
	}

	public String getSerr() {
		return serr;
	}

	public void setSerr(String serr) {
		this.serr = serr;
	}

	public String toHtml() {
		String sout = getSout().replace("\n", "</br>");
		sout.replace("FINEST:", "<b>FINEST:</b>");
		sout.replace("FINER:", "<b>FINER:</b>");
		sout.replace("FINE:", "<b>FINE:</b>");
		sout.replace("CONFIG:", "<b>CONFIG:</b>");
		sout.replace("INFO:", "<b>INFO:</b>");
		sout.replace("WARNING:", "<b>WARNING:</b>");
		sout.replace("SEVERE:", "<font color=\"red\"><b>SEVERE:</b></font>");

		String serr = getSerr().replace("\n", "<br>");
		serr.replace("FINEST:", "<b>FINEST:</b>");
		serr.replace("FINER:", "<b>FINER:</b>");
		serr.replace("FINE:", "<b>FINE:</b>");
		serr.replace("CONFIG:", "<b>CONFIG:</b>");
		serr.replace("INFO:", "<b>INFO:</b>");
		serr.replace("WARNING:", "<b>WARNING:</b>");
		serr.replace("SEVERE:", "<font color=\"red\"><b>SEVERE:</b></font>");

		String html = "<h4>Process Log</h4><br>" + "<b>sout:<br></b> " + sout
				+ "<br>" + "<b>serr:<br></b> " + serr + "<br>";
		return html;
	}

}
