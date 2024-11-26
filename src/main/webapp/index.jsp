<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Top 10 Contents</title>
    <style>
        body {
            font-family: Arial, sans-serif;
        }
        .content-list {
            list-style-type: none;
            padding: 0;
        }
        .content-item {
            margin-bottom: 15px;
            padding: 10px;
            border: 1px solid #ccc;
            border-radius: 5px;
        }
        .content-item h2 {
            margin: 0 0 5px 0;
        }
        .show-button {
            margin-top: 20px;
            padding: 10px;
            border: none;
            background-color: #007BFF;
            color: white;
            border-radius: 5px;
            cursor: pointer;
        }
        .alert-success {
            color: green;
            background-color: #DFF2BF;
            padding: 10px;
            border: 1px solid green;
            border-radius: 5px;
            margin-bottom: 15px;
        }
    </style>
    <script type="text/javascript">
        document.addEventListener("DOMContentLoaded", function() {
           // console.log("DOM fully loaded and parsed");

            const successMessageDiv = document.getElementById('successMessage');
            if (successMessageDiv) {
                console.log("Success message found. Setting timeout to hide it.");
                setTimeout(function() {
                    successMessageDiv.style.display = 'none';
                    //console.log("Success message hidden after 2 seconds");
                }, 2000); // 2000 milliseconds = 2 seconds
            } else {
                console.log("No success message found.");
            }

            const showButton = document.querySelector('.show-button');
            const contentList = document.querySelector('.content-list');
            if (showButton && contentList) {
                showButton.addEventListener('click', function() {
                    //console.log("Show List button clicked");
                    fetch('FileContent?action=list')
                        .then(response => {
                            if (!response.ok) {
                                throw new Error('Network response was not ok');
                            }
                            return response.json();
                        })
                        .then(data => {
                            console.log("Fetched data:", data);
                            contentList.innerHTML = ""; // Clear any existing content
                            contentList.style.display = 'block'; // Ensure the list is displayed

                            data.forEach(content => {
                                console.log("Processing content:", content);
                                const listItem = document.createElement('li');
                                listItem.className = 'content-item';
                                listItem.innerHTML = '<h2>' + content.fileName + '</h2>' +
                                    '<p>Uploaded on: ' + content.formattedUploadTime + '</p>' +
                                    '<a href="FileContent?action=view&filePath=' + content.fileName + '" target="_blank">View File</a>';
                                //console.log("Appending list item:", listItem);
                                contentList.appendChild(listItem);
                            });
                        })
                        .catch(error => console.error('Error fetching data:', error));
                });
            } else {
                console.error("Show button or content list not found");
            }
        });
    </script>
</head>
<body>

<% String successMessage = (String) session.getAttribute("successMessage");
    if (successMessage != null) { %>
<div id="successMessage" class="alert alert-success">
    <%= successMessage %>
</div>
<% session.removeAttribute("successMessage"); %>
<% } %>

<form action="FileUploadServlet" method="post" enctype="multipart/form-data">
    <label for="file">Select File to Upload:</label>
    <input type="file" name="file" id="file">
    <br>
    <input type="submit" value="Upload">
</form>

<button class="show-button">Show List</button>
<h1>Top 10 Contents</h1>
<ul class="content-list">
    <!-- Content will be dynamically inserted here -->
</ul>
</body>
</html>
