#!/usr/bin/env groovy
@GrabResolver(name='ftp-devel', root='http://ftp-devel.mzk.cz/mvnrepo/')
@Grab(group='com.jcraft', module='jsch', version='0.1.53')
@Grab(group='cz.mzk.rajhrad', module='rajhrad', version='1.0')
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import com.jcraft.jsch.ChannelSftp

def MARC_PASSWORD = "tij-mymriz"


JSch jsch = new JSch()
jsch.setConfig("StrictHostKeyChecking", "no")
Session session = jsch.getSession("rumanekm", "aleph.mzk.cz")
session.setPassword(MARC_PASSWORD)
session.connect()
sftp = session.openChannel("sftp")
sftp.connect()
ChannelSftp sftpChannel = (ChannelSftp) sftp;
InputStream is = sftpChannel.get("/work/aleph/data/aktualizace_zaznamu/aktualizace_zaznamu/mzk03.m21")
String fileContents = new File(is)
