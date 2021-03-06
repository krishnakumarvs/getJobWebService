import static spark.Spark.*;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.util.Collection;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Server {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Started............ ");

		staticFiles.externalLocation(Constants.external_file_location);

		options("/*",
				(request, response) -> {

					String accessControlRequestHeaders = request
							.headers("Access-Control-Request-Headers");
					if (accessControlRequestHeaders != null) {
						response.header("Access-Control-Allow-Headers",
								accessControlRequestHeaders);
					}

					String accessControlRequestMethod = request
							.headers("Access-Control-Request-Method");
					if (accessControlRequestMethod != null) {
						response.header("Access-Control-Allow-Methods",
								accessControlRequestMethod);
					}

					return "OK";
				});

		before((request, response) -> response.header(
				"Access-Control-Allow-Origin", "*"));

		// code starts from here....

		get("/hello", (req, res) -> "Hello World");

		post("/samplePost", (request, response) -> {
			return "ok";
		});

		post("/getAllCompaniesOld", (request, response) -> {
			String body = request.body();
			System.out.println(body);
			JSONObject responseData = new JSONObject();

			JSONParser jsonParser = new JSONParser();
			try {
				JSONObject jsonData = (JSONObject) jsonParser.parse(body);
				String userName = jsonData.get("userName").toString();
			} catch (ParseException e) {
				System.out.println("Error in parseing json data");
				System.out.println(e);
				responseData.put("result", false);
				responseData.put("description", "Please send a valid json");
			}
			return "ok";
		});

		post("/login",
				(request, response) -> {
					System.out.println(request.body() + "---");
					String body = request.body();

					JSONObject responseData = new JSONObject();

					System.out.println("received data as " + body);
					JSONParser jsonParser = new JSONParser();

					try {
						JSONObject jsonData = (JSONObject) jsonParser
								.parse(body);
						System.out.println("Data is parsed sucess ");

						if (jsonData.get("username") == null
								|| jsonData.get("password") == null) {
							responseData.put("result", false);
							responseData.put("description",
									"Please send username and password");
						} else {
							String userName = (String) jsonData.get("username");
							String password = (String) jsonData.get("password");
							JSONObject payload = new JSONObject();

							Dbcon db = new Dbcon();
							String sql = "select * from tbl_userview where email_id='"
									+ userName
									+ "' and password='"
									+ password
									+ "'";
							ResultSet rs = db.select(sql);
							if (rs.next()) {
								responseData.put("result", true);
								responseData.put("description",
										"Login was sucess");
								payload.put("name", rs.getString("name"));
								payload.put("email_id",
										rs.getString("email_id"));
								payload.put("phone", rs.getString("phone"));

								payload.put("userId", rs.getString("id"));
								payload.put("address", rs.getString("address"));
								payload.put("dob", rs.getString("dob"));
								payload.put("age", rs.getString("age"));
								payload.put("qualification",
										rs.getString("qualification"));
								payload.put("experience",
										rs.getString("experience"));
								payload.put("photo", rs.getString("photo"));
								payload.put("resume", rs.getString("resume"));

								responseData.put("payload", payload);
							} else {
								responseData.put("result", false);
								responseData.put("description",
										"Login failed, Incorrect credentials");
							}
						}
					} catch (ParseException pe) {
						System.out.println("Error in parseing json data");
						System.out.println(pe);
						responseData.put("result", false);
						responseData.put("description",
								"Please send a valid json");
					}

					return responseData;
				});

		post("/changePassword",
				(request, response) -> {
					System.out.println("changePassword API called --- ");
					String body = request.body();
					JSONObject responseData = new JSONObject();

					System.out.println("received data as " + body);
					JSONParser jsonParser = new JSONParser();

					try {
						JSONObject jsonData = (JSONObject) jsonParser
								.parse(body);
						System.out.println("Data is parsed sucess ");

						if (jsonData.get("currentPass") == null
								|| jsonData.get("newPass") == null
								|| jsonData.get("userId") == null) {

							responseData.put("result", false);
							responseData.put("description",
									"Please send all the values");
						} else {
							String currentPass = (String) jsonData.get("currentPass");
							String newPass = (String) jsonData.get("newPass");
							String userId = (String) jsonData.get("userId");
							JSONObject payload = new JSONObject();

							Dbcon db = new Dbcon();
							String sql = "select * from tbl_userview where password='"+currentPass+"' and id='"+userId+"'";
							ResultSet rs = db.select(sql);
							if (rs.next()) {
								
								new Dbcon()
										.update("update tbl_userview set password='"
												+ newPass
												+ "' where id='"
												+ userId+ "'");
								
								
								responseData.put("result", true);
								responseData
										.put("description",
												"Password is changed");
							} else {
								responseData.put("result", false);
								responseData.put("description",
										"Sorry, current password is wrong");
							}
						}
					} catch (ParseException pe) {
						System.out.println("Error in parseing json data");
						System.out.println(pe);
						responseData.put("result", false);
						responseData.put("description",
								"Please send a valid json");
					}

					return responseData;
				});

		post("/forgotPassword",
				(request, response) -> {
					System.out.println("forgotPassword API called --- ");
					String body = request.body();
					JSONObject responseData = new JSONObject();

					System.out.println("received data as " + body);
					JSONParser jsonParser = new JSONParser();

					try {
						JSONObject jsonData = (JSONObject) jsonParser
								.parse(body);
						System.out.println("Data is parsed sucess ");

						if (jsonData.get("name") == null
								|| jsonData.get("email_id") == null) {
							responseData.put("result", false);
							responseData.put("description",
									"Please send name and email id");
						} else {
							String name = (String) jsonData.get("name");
							String email_id = (String) jsonData.get("email_id");
							JSONObject payload = new JSONObject();

							Dbcon db = new Dbcon();
							String sql = "select * from tbl_userview where email_id='"
									+ email_id + "' and name='" + name + "'";
							ResultSet rs = db.select(sql);
							if (rs.next()) {
								String newPassword = MailSender
										.generatePassword(5);
								new Dbcon()
										.update("update tbl_userview set password='"
												+ newPassword
												+ "' where email_id='"
												+ email_id + "'");
								String messageBody = "Hi, you have requested to change password, and your new password will be "
										+ newPassword
										+ ". Please log in and change password immediately";
								String to[] = { email_id };
								MailSender.sendFromGMail(to,
										"Get Job - Forgot password ",
										messageBody);
								responseData.put("result", true);
								responseData
										.put("description",
												"Password is reseted, please check your email");
							} else {
								responseData.put("result", false);
								responseData.put("description",
										"No such account could be found");
							}
						}
					} catch (ParseException pe) {
						System.out.println("Error in parseing json data");
						System.out.println(pe);
						responseData.put("result", false);
						responseData.put("description",
								"Please send a valid json");
					}

					return responseData;
				});

		post("/editUserDetails",
				(request, response) -> {
					System.out.println("editUserDetails  API call "
							+ request.body() + " --- end ");
					String body = request.body();

					JSONObject responseData = new JSONObject();
					JSONParser jsonParser = new JSONParser();
					JSONObject payload = new JSONObject();

					try {
						JSONObject jsonData = (JSONObject) jsonParser
								.parse(body);

						if (jsonData.get("userId") == null
								|| jsonData.get("name") == null
								|| jsonData.get("address") == null
								|| jsonData.get("email_id") == null
								|| jsonData.get("dob") == null
								|| jsonData.get("experience") == null
								|| jsonData.get("qualification") == null
								|| jsonData.get("resume") == null
								|| jsonData.get("phone") == null) {

							responseData.put("result", false);
							responseData.put("description",
									"Please send all the user details");
						} else {
							Dbcon db = new Dbcon();

							String pic = "";
							if (jsonData.get("pic") != null) {
								pic = jsonData.get("pic") + "";
							}

							String resume = "";
							if (jsonData.get("resume") != null) {
								resume = jsonData.get("resume") + "";
							}
							String sql = "update tbl_userview set name='"
									+ jsonData.get("name") + "' , address='"
									+ jsonData.get("address") + "' , phone='"
									+ jsonData.get("phone") + "' , dob='"
									+ jsonData.get("dob")
									+ "' , qualification='"
									+ jsonData.get("qualification")
									+ "', email_id='"
									+ jsonData.get("email_id")
									+ "', experience='"
									+ jsonData.get("experience")
									+ "' , photo='" + pic + "', resume='"
									+ resume + "'" + " where id = '"
									+ jsonData.get("userId") + "'";

							int update = db.update(sql);
							if (update <= 0) {
								responseData.put("result", false);
								responseData
										.put("description",
												"Could not update now, Please try again later");
							} else {

								sql = "select * from tbl_userview where id="
										+ jsonData.get("userId");
								ResultSet rs = db.select(sql);

								if (rs.next()) {
									responseData.put("result", true);
									responseData
											.put("description",
													"user details updated successfully");
									payload.put("name", rs.getString("name"));
									payload.put("name", rs.getString("name"));
									payload.put("email_id",
											rs.getString("email_id"));
									payload.put("phone", rs.getString("phone"));

									payload.put("userId", rs.getString("id"));
									payload.put("address",
											rs.getString("address"));
									payload.put("dob", rs.getString("dob"));
									payload.put("age", rs.getString("age"));
									payload.put("qualification",
											rs.getString("qualification"));
									payload.put("experience",
											rs.getString("experience"));
									payload.put("photo", rs.getString("photo"));
									payload.put("resume",
											rs.getString("resume"));
									responseData.put("payload", payload);
								} else {
									responseData.put("result", false);
									responseData
											.put("description",
													"Could not update now, Please try again later");
								}

							}
						}
					} catch (ParseException pe) {
						System.out.println("Error in parseing json data");
						System.out.println(pe);
						responseData.put("result", false);
						responseData.put("description",
								"Please send a valid json");
					}

					return responseData;
				});

		post("/registration",
				(request, response) -> {
					System.out.println("registration  API call "
							+ request.body() + " --- end ");
					String body = request.body();

					JSONObject responseData = new JSONObject();
					JSONParser jsonParser = new JSONParser();
					JSONObject payload = new JSONObject();

					try {
						JSONObject jsonData = (JSONObject) jsonParser
								.parse(body);

						if (jsonData.get("name") == null
								|| jsonData.get("email_id") == null
								|| jsonData.get("password") == null) {

							responseData.put("result", false);
							responseData.put("description",
									"Please send all the user details");
						} else {
							Dbcon db = new Dbcon();

							ResultSet rsss = new Dbcon()
									.select("select * from tbl_userview where email_id='"
											+ jsonData.get("email_id") + "'");

							if (rsss.next()) {
								responseData.put("result", false);
								responseData.put("description",
										"Sorry, Email Id already registered");
							} else {
								String sql = "insert into tbl_userview (name,email_id,password) values('"
										+ jsonData.get("name")
										+ "' ,"
										+ " '"
										+ jsonData.get("email_id")
										+ "' , '"
										+ jsonData.get("password") + "' )";

								int update = db.update(sql);
								if (update <= 0) {
									responseData.put("result", false);
									responseData
											.put("description",
													"Could not update now, Please try again later");
								} else {

									sql = "SELECT * FROM tbl_userview WHERE id = (SELECT MAX(id) FROM tbl_userview)";
									ResultSet rs = db.select(sql);

									if (rs.next()) {
										responseData.put("result", true);
										responseData.put("description",
												"Registration success");
										payload.put("name",
												rs.getString("name"));
										payload.put("email_id",
												rs.getString("email_id"));
										payload.put("phone",
												rs.getString("phone"));

										payload.put("userId",
												rs.getString("id"));
										payload.put("address",
												rs.getString("address"));
										payload.put("dob", rs.getString("dob"));
										payload.put("age", rs.getString("age"));
										payload.put("qualification",
												rs.getString("qualification"));
										payload.put("experience",
												rs.getString("experience"));
										payload.put("photo",
												rs.getString("photo"));
										payload.put("resume",
												rs.getString("resume"));
										responseData.put("payload", payload);
									} else {
										responseData.put("result", false);
										responseData
												.put("description",
														"Could not update now, Please try again later");
									}

								}
							}
						}
					} catch (ParseException pe) {
						System.out.println("Error in parseing json data");
						System.out.println(pe);
						responseData.put("result", false);
						responseData.put("description",
								"Please send a valid json");
					}

					return responseData;
				});

		post("/upload",
				"multipart/form-data",
				(request, response) -> {
					// - Servlet 3.x config
				try {
					String location = Constants.external_file_location;
					long maxFileSize = 100000000;
					long maxRequestSize = 100000000;
					int fileSizeThreshold = 1024;

					MultipartConfigElement multipartConfigElement = new MultipartConfigElement(
							location, maxFileSize, maxRequestSize,
							fileSizeThreshold);
					request.raw().setAttribute(
							"org.eclipse.jetty.multipartConfig",
							multipartConfigElement);

					Collection<Part> parts = request.raw().getParts();
					for (Part part : parts) {

						System.out.println("-------------------------");
						System.out.println(part);
						System.out.println("Name:");
						System.out.println(part.getName());
						System.out.println("Size: ");
						System.out.println(part.getSize());
						System.out.println("Filename:");
						System.out.println(part.getSubmittedFileName());
					}

					System.out.println("request.body() ************** "
							+ request.body());

					String fName = request.raw().getPart("upfile")
							.getSubmittedFileName();
					System.out.println("Title: "
							+ request.raw().getParameter("title"));
					System.out.println("File: " + fName);

					Part uploadedFile = request.raw().getPart("upfile");
					Path out = Paths.get(Constants.external_file_location
							+ request.raw().getParameter("title"));
					try (final InputStream in = uploadedFile.getInputStream()) {
						Files.copy(in, out);
						uploadedFile.delete();
					}
					// cleanup
					multipartConfigElement = null;
					parts = null;
					uploadedFile = null;
				} catch (Exception e) {
					e.printStackTrace();
				}

				return "OK";
			});

		post("/upload_resume",
				"multipart/form-data",
				(request, response) -> {
					// - Servlet 3.x config
				try {
					String location = Constants.external_file_location;
					long maxFileSize = 100000000;
					long maxRequestSize = 100000000;
					int fileSizeThreshold = 1024;

					MultipartConfigElement multipartConfigElement = new MultipartConfigElement(
							location, maxFileSize, maxRequestSize,
							fileSizeThreshold);
					request.raw().setAttribute(
							"org.eclipse.jetty.multipartConfig",
							multipartConfigElement);

					Collection<Part> parts = request.raw().getParts();
					for (Part part : parts) {

						System.out.println("-------------------------");
						System.out.println(part);
						System.out.println("Name:");
						System.out.println(part.getName());
						System.out.println("Size: ");
						System.out.println(part.getSize());
						System.out.println("Filename:");
						System.out.println(part.getSubmittedFileName());
					}

					System.out.println("request.body() ************** "
							+ request.body());

					String fName = request.raw().getPart("upfile")
							.getSubmittedFileName();
					System.out.println("Title: "
							+ request.raw().getParameter("title"));
					System.out.println("File: " + fName);

					Part uploadedFile = request.raw().getPart("upfile");
					Path out = Paths.get(Constants.external_file_location
							+ request.raw().getParameter("title"));
					try (final InputStream in = uploadedFile.getInputStream()) {
						Files.copy(in, out);
						uploadedFile.delete();
					}
					// cleanup
					multipartConfigElement = null;
					parts = null;
					uploadedFile = null;
				} catch (Exception e) {
					e.printStackTrace();
				}

				return "OK";
			});

		post("/deleteNotification", (request, response) -> {
			System.out.println("getAnnouncement  API call " + request.body()
					+ " --- end ");
			String body = request.body();

			JSONObject responseData = new JSONObject();
			JSONParser jsonParser = new JSONParser();

			try {
				JSONObject jsonData = (JSONObject) jsonParser.parse(body);

				// DELETE
				/* jsonData.put("userId", "1"); */

				if (jsonData.get("notificationId") == null) {
					responseData.put("result", false);
					responseData.put("description",
							"Please send notification Id");
				} else {
					JSONObject payload = new JSONObject();
					Dbcon db = new Dbcon();

					String sql = "delete from tbl_notification where id='"
							+ jsonData.get("notificationId") + "'";

					int upt = db.update(sql);
					if (upt > 0) {
						responseData.put("result", true);
						responseData.put("description",
								"Sucessfully deleted notification");
					} else {
						responseData.put("result", false);
						responseData.put("description",
								"Could not delete notification");
					}

				}
			} catch (ParseException pe) {
				System.out.println("Error in parseing json data");
				System.out.println(pe);
				responseData.put("result", false);
				responseData.put("description", "Please send a valid json");
			} catch (Exception e) {
				e.printStackTrace();
			}

			return responseData;
		});

		post("/deleteMessage",
				(request, response) -> {
					System.out.println("deleteMessage API call "
							+ request.body() + " --- end ");
					String body = request.body();

					JSONObject responseData = new JSONObject();
					JSONParser jsonParser = new JSONParser();

					try {
						JSONObject jsonData = (JSONObject) jsonParser
								.parse(body);

						// DELETE
						/* jsonData.put("userId", "1"); */

						if (jsonData.get("messageId") == null) {
							responseData.put("result", false);
							responseData.put("description",
									"Please send message Id");
						} else {
							JSONObject payload = new JSONObject();
							Dbcon db = new Dbcon();

							String sql = "delete from tbl_message where id="
									+ jsonData.get("messageId") + "";

							int upt = db.update(sql);
							if (upt > 0) {
								responseData.put("result", true);
								responseData.put("description",
										"Sucessfully deleted message");
							} else {
								responseData.put("result", false);
								responseData.put("description",
										"Could not delete message");
							}

						}
					} catch (ParseException pe) {
						System.out.println("Error in parseing json data");
						System.out.println(pe);
						responseData.put("result", false);
						responseData.put("description",
								"Please send a valid json");
					} catch (Exception e) {
						e.printStackTrace();
					}

					return responseData;
				});

		post("/getAnnouncement",
				(request, response) -> {
					System.out.println("getAnnouncement  API call "
							+ request.body() + " --- end ");
					String body = request.body();

					JSONObject responseData = new JSONObject();
					JSONParser jsonParser = new JSONParser();

					try {
						JSONObject jsonData = (JSONObject) jsonParser
								.parse(body);

						// DELETE
						/* jsonData.put("userId", "1"); */

						if (jsonData.get("userId") == null) {
							responseData.put("result", false);
							responseData.put("description",
									"Please send user ID");
						} else {
							JSONObject payload = new JSONObject();
							JSONArray dataarray = new JSONArray();
							Dbcon db = new Dbcon();

							String sql = "SELECT *,ann.id as annId  FROM tbl_announcement AS ann , tbl_company  AS comp WHERE ann.companyId = comp.id";
							ResultSet rs = db.select(sql);
							while (rs.next()) {
								JSONObject notify = new JSONObject();

								notify.put("post", rs.getString("post"));
								notify.put("id", rs.getString("annId"));
								notify.put("description",
										rs.getString("discription"));
								notify.put("name", rs.getString("name"));
								notify.put("address", rs.getString("address"));
								notify.put("qualification",
										rs.getString("qualification"));
								notify.put("age_limit",
										rs.getString("age_limit"));
								notify.put("experience",
										rs.getString("experience"));
								notify.put("last_date",
										rs.getString("date_in_milli"));
								notify.put("salary",
										rs.getString("salary"));

								dataarray.add(notify);
							}
							responseData.put("result", true);
							responseData.put("description",
									"Sucessfully fetched ");
							responseData.put("payload", dataarray);
						}
					} catch (ParseException pe) {
						System.out.println("Error in parseing json data");
						System.out.println(pe);
						responseData.put("result", false);
						responseData.put("description",
								"Please send a valid json");
					} catch (Exception e) {
						e.printStackTrace();
					}

					return responseData;
				});

		post("/getAllCompanies", (request, response) -> {
			System.out.println("getAllCompanies  API call " + request.body()
					+ " --- end ");
			String body = request.body();

			JSONObject responseData = new JSONObject();
			JSONParser jsonParser = new JSONParser();

			try {
				JSONObject jsonData = (JSONObject) jsonParser.parse(body);

				// DELETE
				/* jsonData.put("userId", "1"); */

				if (false) {
					responseData.put("result", false);
					responseData.put("description", "Please send user ID");
				} else {
					JSONObject payload = new JSONObject();
					JSONArray dataarray = new JSONArray();
					Dbcon db = new Dbcon();

					String sql = "SELECT * from tbl_company";
					ResultSet rs = db.select(sql);
					while (rs.next()) {
						JSONObject notify = new JSONObject();

						notify.put("id", rs.getString("id"));

						notify.put("name", rs.getString("name"));
						notify.put("website", rs.getString("website"));
						notify.put("mail_id", rs.getString("mail_id"));
						notify.put("phone_no", rs.getString("phone_no"));
						notify.put("address", rs.getString("address"));
						notify.put("description", rs.getString("discription"));
						notify.put("photo", rs.getString("photo"));

						dataarray.add(notify);
					}
					responseData.put("result", true);
					responseData.put("description", "Sucessfully fetched ");
					responseData.put("payload", dataarray);
				}
			} catch (ParseException pe) {
				System.out.println("Error in parseing json data");
				System.out.println(pe);
				responseData.put("result", false);
				responseData.put("description", "Please send a valid json");
			} catch (Exception e) {
				e.printStackTrace();
			}

			return responseData;
		});

		post("/getRecentInterviewDates",
				(request, response) -> {
					System.out.println("getRecentInterviewDates  API call "
							+ request.body() + " --- end ");
					String body = request.body();

					JSONObject responseData = new JSONObject();
					JSONParser jsonParser = new JSONParser();

					try {
						JSONObject jsonData = (JSONObject) jsonParser
								.parse(body);

						if (jsonData.get("userId") == null) {

							responseData.put("result", false);
							responseData.put("description",
									"Please send semester ");
						} else {
							JSONObject payload = new JSONObject();
							Dbcon db = new Dbcon();

							String sql = "SELECT msg.*, cmp.name AS cname FROM tbl_message AS msg,tbl_company AS cmp  WHERE msg.company_id = cmp.id AND msg.date_milli IS NOT NULL AND msg.userid="
									+ jsonData.get("userId")
									+ "";
							System.out.println(sql);
							ResultSet rs = db.select(sql);
							JSONArray jarray = new JSONArray();
							while (rs.next()) {
								
								String interviewDateMilliString = rs
										.getString("date_milli");

								long interviewDateMilli = Long
										.parseLong(interviewDateMilliString);

								long oneDay = 24 * 60 * 60 * 1000;

								long noOfDaysForInterview = interviewDateMilli
										- System.currentTimeMillis();
								System.out.println(" milli sec diff = "
										+ noOfDaysForInterview);

								noOfDaysForInterview = noOfDaysForInterview
										/ oneDay;

								System.out.println("No of days for interview "
										+ noOfDaysForInterview);

								if (noOfDaysForInterview <= 5
										&& noOfDaysForInterview >= 1) {
									JSONObject resutPay = new JSONObject();
									resutPay.put("noOfDaysForInterview",
											noOfDaysForInterview);

									resutPay.put(
											"description",
											"You have on interview at "
													+ rs.getString("cname")
													+ " in "
													+ noOfDaysForInterview
													+ " days");

									jarray.add(resutPay);

								}
							}

							responseData.put("result", true);
							responseData.put("description",
									"Sucessfully fetched");
							responseData.put("payload", jarray);
						}
					} catch (ParseException pe) {
						System.out.println("Error in parseing json data");
						System.out.println(pe);
						responseData.put("result", false);
						responseData.put("description",
								"Please send a valid json");
					} catch (Exception e) {
						e.printStackTrace();
					}

					return responseData;
				});

		post("/getNotifications",
				(request, response) -> {
					System.out.println("getNotifications  API call "
							+ request.body() + " --- end ");
					String body = request.body();

					JSONObject responseData = new JSONObject();
					JSONParser jsonParser = new JSONParser();

					try {
						JSONObject jsonData = (JSONObject) jsonParser
								.parse(body);

						// DELETE
						/* jsonData.put("userId", "1"); */

						if (jsonData.get("userId") == null) {
							responseData.put("result", false);
							responseData.put("description",
									"Please send user ID");
						} else {
							JSONObject payload = new JSONObject();
							JSONArray dataarray = new JSONArray();
							Dbcon db = new Dbcon();

							String sql = "SELECT * FROM tbl_notification where userid='"
									+ jsonData.get("userId")
									+ "' order by id desc"; // add where
							// conditions
							// to show
							// notification
							// only for
							// articular
							// user
							ResultSet rs = db.select(sql);
							while (rs.next()) {
								JSONObject notify = new JSONObject();

								notify.put("title", rs.getString("title"));
								notify.put("id", rs.getString("id"));
								notify.put("description",
										rs.getString("description"));

								dataarray.add(notify);
							}
							responseData.put("result", true);
							responseData.put("description",
									"Sucessfully fetched ");
							responseData.put("payload", dataarray);
						}
					} catch (ParseException pe) {
						System.out.println("Error in parseing json data");
						System.out.println(pe);
						responseData.put("result", false);
						responseData.put("description",
								"Please send a valid json");
					} catch (Exception e) {
						e.printStackTrace();
					}

					return responseData;
				});

		post("/getAllMessages",
				(request, response) -> {
					System.out.println("getAllMessages  API call "
							+ request.body() + " --- end ");
					String body = request.body();

					JSONObject responseData = new JSONObject();
					JSONParser jsonParser = new JSONParser();

					try {
						JSONObject jsonData = (JSONObject) jsonParser
								.parse(body);

						// DELETE
						/* jsonData.put("userId", "1"); */

						if (jsonData.get("userId") == null) {
							responseData.put("result", false);
							responseData.put("description",
									"Please send user ID");
						} else {
							JSONObject payload = new JSONObject();
							JSONArray dataarray = new JSONArray();
							Dbcon db = new Dbcon();

							String sql = "SELECT msg.*, comp.name as companyName FROM tbl_message as msg , tbl_company as comp where msg.userid='"
									+ jsonData.get("userId")
									+ "' and msg.company_id=comp.id order by msg.id desc"; // add
																							// where
							// conditions
							// to show
							// messages
							// only for
							// articular
							// user
							ResultSet rs = db.select(sql);
							while (rs.next()) {
								JSONObject notify = new JSONObject();

								notify.put("title", rs.getString("title"));
								notify.put("id", rs.getString("id"));
								notify.put("description",
										rs.getString("discription"));
								notify.put("interview_date",
										rs.getString("interview_date"));
								notify.put("times", rs.getString("times"));
								notify.put("companyName",
										rs.getString("companyName"));
								dataarray.add(notify);
							}
							responseData.put("result", true);
							responseData.put("description",
									"Sucessfully fetched ");
							responseData.put("payload", dataarray);
						}
					} catch (ParseException pe) {
						System.out.println("Error in parseing json data");
						System.out.println(pe);
						responseData.put("result", false);
						responseData.put("description",
								"Please send a valid json");
					} catch (Exception e) {
						e.printStackTrace();
					}

					return responseData;
				});

		post("/getMessages",
				(request, response) -> {
					System.out.println("getMessages  API call "
							+ request.body() + " --- end ");
					String body = request.body();

					JSONObject responseData = new JSONObject();
					JSONParser jsonParser = new JSONParser();

					try {
						JSONObject jsonData = (JSONObject) jsonParser
								.parse(body);

						// DELETE
						/* jsonData.put("userId", "1"); */

						if (jsonData.get("userId") == null) {
							responseData.put("result", false);
							responseData.put("description",
									"Please send user ID");
						} else {
							JSONObject payload = new JSONObject();
							JSONArray dataarray = new JSONArray();
							Dbcon db = new Dbcon();

							String sql = "SELECT msg.*, comp.name as cName FROM tbl_message as msg, tbl_company as comp where msg.userid='"
									+ jsonData.get("userId")
									+ "' and msg.company_id=comp.id order by msg.id desc"; // add
																							// where
							// conditions
							// to show
							// notification
							// only for
							// articular
							// user
							ResultSet rs = db.select(sql);
							while (rs.next()) {
								JSONObject notify = new JSONObject();

								notify.put("title", rs.getString("title"));
								notify.put("id", rs.getString("id"));
								notify.put("description",
										rs.getString("discription"));
								notify.put("date",
										rs.getString("interview_date"));
								notify.put("time", rs.getString("times"));
								notify.put("comppanyname",
										rs.getString("cName"));

								dataarray.add(notify);
							}
							responseData.put("result", true);
							responseData.put("description",
									"Sucessfully fetched ");
							responseData.put("payload", dataarray);
						}
					} catch (ParseException pe) {
						System.out.println("Error in parseing json data");
						System.out.println(pe);
						responseData.put("result", false);
						responseData.put("description",
								"Please send a valid json");
					} catch (Exception e) {
						e.printStackTrace();
					}

					return responseData;
				});

		post("/applyJob",
				(request, response) -> {
					System.out.println("applyJob  API call " + request.body()
							+ " --- end ");
					String body = request.body();

					JSONObject responseData = new JSONObject();
					JSONParser jsonParser = new JSONParser();
					JSONObject payload = new JSONObject();

					try {
						JSONObject jsonData = (JSONObject) jsonParser
								.parse(body);

						if (jsonData.get("userId") == null
								|| jsonData.get("announcementId") == null) {

							responseData.put("result", false);
							responseData.put("description",
									"Please send all the details");
						} else {
							Dbcon db = new Dbcon();
							String companyName = "admin";
							if (jsonData.get("companyName") != null) {
								companyName = jsonData.get("companyName") + "";
							}
							String sql1="select * from tbl_request where user_id='"+jsonData.get("userId")+"' and ann_id='"+jsonData.get("announcementId")+"'";
							ResultSet rs1 = db.select(sql1);
							System.out.println(sql1);
							if(rs1.next()){
								responseData.put("result", false);
								responseData.put("description",
										"Already applied");
							}
							else{
								String sql = "insert into tbl_request (user_id,ann_id,date_milli, Status) values('"
									+ jsonData.get("userId")
									+ "' , ' "
									+ jsonData.get("announcementId")
									+ "' , '"
									+ System.currentTimeMillis()
									+ "' , 'Requested')";

							int ins = db.insert(sql);
							if (ins <= 0) {
								responseData.put("result", false);
								responseData
										.put("description",
												"Could not send request now, Please try again later");
							} else {
								responseData.put("result", true);
								responseData.put("description",
										"Apply job successfully");
							}
						}
					}
					} catch (ParseException pe) {
						System.out.println("Error in parseing json data");
						System.out.println(pe);
						responseData.put("result", false);
						responseData.put("description",
								"Please send a valid json");
					}

					return responseData;
				});

		post("/sendFeedback",
				(request, response) -> {
					System.out.println("sendFeedback  API call "
							+ request.body() + " --- end ");
					String body = request.body();

					JSONObject responseData = new JSONObject();
					JSONParser jsonParser = new JSONParser();
					JSONObject payload = new JSONObject();

					try {
						JSONObject jsonData = (JSONObject) jsonParser
								.parse(body);

						if (jsonData.get("userId") == null
								|| jsonData.get("feedbackSubject") == null
								|| jsonData.get("feedbackDescription") == null
								|| jsonData.get("feedbackTo") == null) {

							responseData.put("result", false);
							responseData.put("description",
									"Please send all the details");
						} else {
							Dbcon db = new Dbcon();
							String companyName = "admin";
							if (jsonData.get("companyName") != null) {
								companyName = jsonData.get("companyName") + "";
							}
							String sql = "insert into tbl_feedback (owner, audence, title, discription, feedbackdate, feedback) values('"
									+ jsonData.get("userId")
									+ "' , "
									+ " '"
									+ companyName
									+ "' , '"
									+ jsonData.get("feedbackSubject")
									+ "' , '"
									+ jsonData.get("feedbackDescription")
									+ "', '"
									+ System.currentTimeMillis()
									+ "' , '" + "user" + "' )";

							int ins = db.insert(sql);
							if (ins <= 0) {
								responseData.put("result", false);
								responseData
										.put("description",
												"Could not send feedback now, Please try again later");
							} else {
								responseData.put("result", true);
								responseData.put("description",
										"Feedback posted successfully");
							}
						}
					} catch (ParseException pe) {
						System.out.println("Error in parseing json data");
						System.out.println(pe);
						responseData.put("result", false);
						responseData.put("description",
								"Please send a valid json");
					}

					return responseData;
				});

	}
}