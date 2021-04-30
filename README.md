# is708-project
## Package dependency
I provide the requirement.txt to install the dependency automatically.
You can use it by executing:

```
pip install -r requirements.txt 
```

If somehow it does not work, you can try these steps manually:
1. conda create -n is708-project python=3.7 anaconda
2. conda acticate is708-project
3. pip install tsfel
4. pip install flask
5. pip install opencv-python
6. pip install cvlib
7. pip install tensorflow
8. pip install spacy
9. python -m spacy download en_core_web_sm

## Server instruction:
1. Set the ip in the server in server_api.python
2. Execute below command in your prompt

```
set FLASK_APP=server_api.python
set FLASK_ENV={your environment name}
flask run --host={your ip address}
```

## Android manual instruction:
Make sure that the IP configuration in the android application is correct. 
You can change the IP in CommModule.java

1. Open the app
2. Press record button while speaking
3. Wait around 3 seconds, and the bounding box will appear
4. If somehow the bounding box does not appear, check the error message in the screen
5. Send gesture by pressing up or down button
6. Wait around 2 seconds
7. After the gesture appear, if you want to give another command voice, just clear the screen using reset button
8. Repeat step number 2



