This is an attempt to create a basic video streaming backend service which can be used for uploading and video streaming purpose.
It includes the following:
1) Uploading a video and saving it entirely in raw form. Not recommended
2) Streaming a video in entirety. Not recommended.
3) Streaming a video in chunks of 1 MB but that can be customised. Not Recommended but little bit better than sending the whole video resource to client.
4) Using HLS to create master and segments for each uploaded vide and serving those segments later on.
5) Creating HLS playlist which supports 4 resolutions currently(360p,480p,720p,1080p) and serving these playlists.
