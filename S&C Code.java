

//Snippets of code writtten at S&C Electric, with logic, convention, and variable names changed 
//as to comply with the Confidentiality Agreement.


     private void met(int taskNumber) throws JSONException {
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        a = new JSONObject();
        mTaskLocationArray = new JSONArray();
        mTaskLocation = new JSONObject();
        if (loc != null) {
            mTaskLocation.put("latitude", loc.getLatitude());
            mTaskLocation.put("longitude", loc.getLongitude());
        }
        mTaskLocationArray.put(0, mTaskLocation);
        a.put(taskNumber, a.put("taskLocation", (JSONArray) mTaskLocationArray));
    }

      private void refreshTaskPage() {
       
        //If somehow the current assignment we are on is higher than the number of assignments reset it to zero
        if (mCurrent >= mDevices.size()) {
            mCurrent = 0;
        }

        //Retrieve the tasks associated with this instance/assignment
        if (mDevices.size() > mCurrent)
            mTasks = mDevices.get(mCurrent).getWorkTasks();
        else
            mTasks = new ArrayList<WorkTask>(); //reset mTasks to be blank

        //Setup UI to display mTasks and other things properly after being retrieved from above
        setupPageItems();

        if (mTasks.size() == 0) { /* There are no tasks to be done -- Change UI to reflect so */
            mStartTasks.setBackgroundColor(getResources().getColor(R.color.snc_darkgrey));
            mStartTasks.setText(getString(R.string.no_tasks));
        } else { /* Turn the button to do Start */
            setButtonToStart(getString(R.string.task_button_start));
        }

        //Set the work status status to be open since we are on the first work task -- should this happen if we have no tasks?
        mSucceeded = WorkTask.STATUS_OPEN;

        if (mSpinner != null) {//setup up dropdownlist and enables users to click on dropdownlist
            mSpinner.setEnabled(true);
            mSpinner.setClickable(true);
            mSpinner.setOnItemSelectedListener(mTaskSpinnerListener);
        }
    }
 private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "PNG_" + mDevices.get(mCurrent).getAssetId() + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".PNG",         /* suffix */
                storageDir      /* directory */
        );
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ContextWrapper contextWrapper = new ContextWrapper(mContext);
        File directory = contextWrapper.getDir(mDevices.get(mCurrent).getAssetId(), Context.MODE_PRIVATE);
        File internalFile = new File(directory, outputFile.getName());
        System.out.println("OUT" + outputFile.length());

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {

            InputStream is = null;
            try {
                is = new FileInputStream(outputFile);
                OutputStream outStream = new FileOutputStream(internalFile);
                byte fileBytes[] = new byte[1024];
                int readSize;
                while ((readSize = is.read(fileBytes)) > 0) {
                    System.out.println("COPY PHOTO FILE " + readSize);
                    outStream.write(fileBytes, 0, readSize);
                    outStream.flush();
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            outputFile.delete();
        }
    }


    /**
     * Prompts user to write notes when there are no notes
     */
    private void promptNotes() {
        mAlertDialogBuilder = new AlertDialog.Builder(mContext);
        mAlertDialogBuilder.setTitle("Notes");
        mAlertDialogBuilder.setMessage("You have not written any notes. Would you like to write some notes?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        writeNotes(true);
                        dialog.cancel();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            closeAssignment();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        dialog.cancel();
                    }
                });
        mDialog = mAlertDialogBuilder.create();
        mDialog.show();
    }

    /**
     * User inputs notes into a dialog
     *
     * @param function the type of writeNotes: false = normal write notes; true = close task after writing notes
     */
    private void writeNotes(boolean function) {
        final boolean temp = function;
        mAlertDialogBuilder = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        mAlertDialogBuilder.setTitle("Write Notes");
        mAlertDialogBuilder.setView(input);
        mAlertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mDevices.get(mCurrent).setLineWorkerNotes(input.getText().toString());
                if (temp) {
                    try {
                        closeAssignment();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                dialog.cancel();
            }
        });
        mAlertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        mDialog = mAlertDialogBuilder.create();
        mDialog.show();
    }


    /**
     * saves the JSON files that the metadata is stored in, to a file in a folder so that it syncs.
     *
     * @throws IOException
     */
    private void saveDataToFile() throws IOException {
        if (mTasks.size() <= 0 || mTasks == null) {
            savedToFile = true;
        }

        //if any task is started and not yet saved to file this statement runs
        if (!savedToFile) {
            Log.d("taskPage", "WRITING METADATA");
            ContextWrapper fileContext = new ContextWrapper(getApplicationContext());
            File directory;
            if (mCurrentTask == 1) {
                directory = fileContext.getDir(mDevices.get(mCurrent).getAssetId(), Context.MODE_PRIVATE); //Find the files in Firmwares
            } else {
                directory = fileContext.getDir(mDevices.get(mCurrent).getAssetId(), Context.MODE_PRIVATE); //Find the files in Firmwares
            }
            FileWriter workTaskFile = null;
            if (mCurrentTask == 1) {
                workTaskFile = new FileWriter(directory + "/" + "metadata_" + mDevices.get(mCurrent).getAssetId() + fDate + ".meta"); //todo: make file based on naming convention (metadata_assetID_timeStampUptoSeconds.txt)
            } else {
                workTaskFile = new FileWriter(directory + "/" + "metadata_" + mDevices.get(mCurrent).getAssetId() + fDate + ".meta"); //todo: make file based on naming convention (metadata_assetID_timeStampUptoSeconds.txt)                }
            }
//                workTaskFile.write("/n/n" + mDevices.get(mCurrent).getWorkTaskGroupName() + "metadata: /n");
            workTaskFile.write(metadataStore.toString());
            Log.d("taskPage", "the path is: " + directory.getAbsolutePath());
            workTaskFile.flush();
            workTaskFile.close();
            //reinitizlize json objects.
            mMetadataArray = new JSONArray();
            metadataStore = new JSONObject();
            savedToFile = true;
        }
    }



     private void mustFinishTask() {
        for (int i = 0; i < AssignmentArray.getInstance().getOpenAssignments().size(); i++) {
            String name = " " + "standardNamingConvetion";
            String temp = AssignmentArray.getInstance().getDueDate(i);
            SimpleDateFormat fromSalesForce = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date current;
            try {
                current = fromSalesForce.parse(temp);
                long dueDate = current.getTime();
                long today = new Date().getTime();
                long result = TimeUnit.DAYS.convert(Math.abs(dueDate - today), TimeUnit.MILLISECONDS);
                if (result < 2) {
                    Intent intent = new Intent(this, TasksPage.class);
                    intent.putExtra("position", i);
                    PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                    Notification notification = new Notification.Builder(this)
                            .setContentTitle(getString(R.string.notification_complete_assignment) + name)
                            .setContentText(Long.toString(result) + getString(R.string.notification_days_left)).setSmallIcon(R.drawable.lineatelogo_notification)
                            .setContentIntent(pIntent).setColor(getResources().getColor(R.color.snc_green)).build();
                    notification.flags |= Notification.FLAG_AUTO_CANCEL;
                    notificationManager.notify(0, notification);
                    break;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }