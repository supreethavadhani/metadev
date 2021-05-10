/*
 * Copyright (c) 2020 simplity.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.simplity.fm.core.conf;

/**
 * utility to send a text message to a mobile no
 * 
 * @author simplity.org
 *
 */
public interface IEmailer {

	/**
	 * send an email. If the toMailIds has more than one ids, then they are all added to the to-recipients, and a single mail is sent.
	 * Refer to sendBulkMails if you want to send the same content as individual mails to several ids.
	 * 
	 * @param toMailIds possibly comma separated list of email ids to which this mail is to be sent to.
	 * @param emailSubject
	 * @param EmailContent
	 */

	void sendEmail(final String toMailIds, final String subject, final String content);
	/**
	 * send an email to the to-address. From address is part of the configuration process.  
	 * 
	 * @param toMailIds possibly comma separated list of email ids to which this mail is to be sent to. Note that the mail is sent individually to each of the recepientIds.
	 * for example, if the toMailIds has tree ids, then three separate mails are sent
	 * @param emailSubject
	 * @param EmailContent
	 */
	void sendBulkEmails(final String toMailIds, final String subject, final String content);
}
