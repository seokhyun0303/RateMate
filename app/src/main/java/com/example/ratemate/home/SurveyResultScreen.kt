package com.example.ratemate.home

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbDownOffAlt
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ThumbUpOffAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.ratemate.R
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar

@Composable
fun SurveyResultScreen(navController: NavHostController) {

    val surveyResult = getExampleSurveyResult()
    val user = getExampleUser()

    val title = surveyResult.title
    val writer = surveyResult.writer
    val content = surveyResult.content
    val userImg = user.userImg
    val userName = user.userName
    var commentList by rememberSaveable { mutableStateOf(surveyResult.comments) }
    var like by remember { mutableIntStateOf(surveyResult.like) }
    var numberOfComment by remember { mutableIntStateOf(surveyResult.comments.size) }
    var sortComment by remember { mutableStateOf("인기순") }
    val context = LocalContext.current


    //해당 유저가 이 결과화면에 들어온적이 있는지 확인 후 없으면 좋아요를 눌렀는지 등 과 같은 정보를 저장할 리스트 생성
    if (surveyResult.surveyResultUserList.find { it.user == userName } == null) {
        surveyResult.surveyResultUserList.add(SurveyResultUser(user = userName))
    }

    //해당 유저가 좋아요를 눌렀는지 확인
    var isLiked by rememberSaveable {
        mutableStateOf(
            surveyResult.surveyResultUserList.find { it.user == userName }?.isLiked ?: false
        )
    }

    //좋아요를 눌렀을때 실행될 함수
    val clickLikes = {
        if (isLiked) {
            like -= 1
            isLiked = false
            surveyResult.surveyResultUserList.find { it.user == userName }?.isLiked = false
        } else {
            like += 1
            isLiked = true
            surveyResult.surveyResultUserList.find { it.user == userName }?.isLiked = true
        }
    }

    //인기순 버튼 클릭시 실행될 함수
    val clickSortByLikes = {
        sortComment = "인기순"
        commentList = commentList.sortedByDescending { it.like - it.dislike }
    }

    //최신순 버튼 클릭시 실행될 함수
    val clickSortByDate = {
        sortComment = "최신순"
        commentList = commentList.sortedByDescending { it.date }
    }

    //댓글 추가 함수
    fun addComment(comment: String) {
        val calendar = Calendar.getInstance()
        val newComment = Comment(
            img = userImg,
            username = userName,
            commentText = comment,
            like = 0,
            dislike = 0,
            date = calendar.time,
            commentUserList = mutableListOf(CommentUser(user = userName, isUsersComment = true))
        )
        commentList = commentList + newComment
        numberOfComment = commentList.size

        if (sortComment == "인기순") {
            clickSortByLikes()
        } else {
            clickSortByDate()
        }

    }


    //댓글 입력 버튼 클릭시 실행될 함수
    val onClickSend = { comment: String ->
        if (comment == "") {
            Toast.makeText(context, "댓글을 입력해주세요", Toast.LENGTH_SHORT).show()
        } else {
            addComment(comment)
            Toast.makeText(context, "댓글이 등록되었습니다", Toast.LENGTH_SHORT).show()
        }
    }


    //전체화면 Column
    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        //상단


        //제목, 작성자
        val modifier1 = Modifier.weight(1f)
        ShowTitle(title = title, writer = writer, modifier = modifier1)

        //내용
        val modifier2 = Modifier.weight(3f)
        ShowMainContent(content = content, modifier = modifier2)

        //좋아요, 댓글 수
        val modifier3 = Modifier.weight(1f)
        ShowCounts(
            isLiked = isLiked,
            like = like,
            numberOfComment = numberOfComment,
            modifier = modifier3,
            clickLikes = clickLikes
        )

        //댓글 정렬 방법 버튼
        Divider(thickness = 3.dp)
        val modifier4 = Modifier.weight(0.8f)
        ShowSortComment(
            sortComment = sortComment,
            modifier = modifier4,
            clickSortByLikes = clickSortByLikes,
            clickSortByDate = clickSortByDate
        )

        //댓글 리스트
        Divider()
        val modifier5 = Modifier.weight(5f)
        ShowComments(username = userName, comments = commentList, modifier = modifier5)

        //댓글 입력창
        val modifier6 = Modifier.weight(1f)
        ShowCommentInput(modifier = modifier6, onClickSend = onClickSend)

        //바텀 네비게이션바


    }

}

@Composable
fun getExampleSurveyResult(): SurveyResult {
    val calendar1 = Calendar.getInstance()
    calendar1.set(2022, 10, 1)
    val commentEX1 = Comment(
        img = painterResource(id = R.drawable.image1),
        username = "작성자1",
        commentText = "내용1",
        like = 100,
        dislike = 3,
        date = calendar1.time,
        commentUserList = mutableListOf(CommentUser(user = "작성자1", isUsersComment = true))
    )

    val calendar2 = Calendar.getInstance()
    calendar2.set(2023, 10, 2)
    val commentEX2 = Comment(
        img = painterResource(id = R.drawable.image2),
        username = "작성자2",
        commentText = "내용2",
        like = 1,
        dislike = 15,
        date = calendar2.time,
        commentUserList = mutableListOf(CommentUser(user = "작성자2", isUsersComment = true))
    )

    val list_of_comments = mutableListOf<Comment>(commentEX1, commentEX2)

    val SurveyResult = SurveyResult(
        title = "제목",
        writer = "작성자",
        content = "내용",
        like = 10,
        comments = list_of_comments,
        surveyResultUserList = mutableListOf()

    )

    return SurveyResult
}

@Composable
fun getExampleUser(): User {
    return User(
        userImg = painterResource(id = R.drawable.logo_only),
        userName = FirebaseAuth.getInstance().currentUser?.email ?: "Guest"
    )
}

@Composable
fun ShowTitle(title: String, writer: String, modifier: Modifier) {
    Column(modifier = modifier) {

        //제목
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        //작성자
        Text(
            text = writer,
            fontSize = 15.sp,
            color = Color.Gray,
            modifier = Modifier
                .padding(start = 5.dp, top = 5.dp, bottom = 5.dp, end = 5.dp)
        )

    }
}

@Composable
fun ShowMainContent(content: String, modifier: Modifier) {
    Box(modifier = modifier) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray.copy(alpha = 0.5f))
                .padding(start = 5.dp, top = 5.dp, bottom = 5.dp, end = 5.dp)
        ) {
            Text(
                text = content,
                fontSize = 15.sp,
                modifier = Modifier.padding(10.dp)
            )
        }


    }
}

@Composable
fun ShowCounts(
    isLiked: Boolean,
    like: Int,
    numberOfComment: Int,
    modifier: Modifier,
    clickLikes: () -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {

        //좋아요 버튼
        Icon(
            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = "Likes",
            modifier = Modifier.clickable {
                clickLikes()
            }
        )

        //좋아요 수
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = "$like likes")


        //댓글 수
        Spacer(modifier = Modifier.width(16.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Comment,
            contentDescription = "Comments"
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = "$numberOfComment comments")


    }
}

@Composable
fun ShowSortComment(
    sortComment: String,
    modifier: Modifier,
    clickSortByLikes: () -> Unit,
    clickSortByDate: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {

        //인기순
        Button(
            onClick = {
                clickSortByLikes()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (sortComment == "인기순") Color.Black else Color.Gray
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp)

        ) {
            Text(text = "인기순")
        }


        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = {
                clickSortByDate()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (sortComment == "최신순") Color.Black else Color.Gray
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(text = "최신순")
        }

    }
}

@Composable
fun ShowComments(username: String, comments: List<Comment>, modifier: Modifier) {
    LazyColumn(
        modifier = modifier
    ) {
        items(comments) { comment ->

            LaunchedEffect(Unit) {
                if (comment.commentUserList.find { it.user == username } == null) {
                    comment.commentUserList.add(CommentUser(user = username))
                }
            }

            var like by rememberSaveable { mutableIntStateOf(comment.like) }
            var dislike by rememberSaveable { mutableIntStateOf(comment.dislike) }
            var isLiked by rememberSaveable {
                mutableStateOf(
                    comment.commentUserList.find { it.user == username }?.isLiked ?: false
                )
            }
            var isDisliked by rememberSaveable {
                mutableStateOf(
                    comment.commentUserList.find { it.user == username }?.isDisliked ?: false
                )
            }

            LaunchedEffect(comments) {
                like = comment.like
                dislike = comment.dislike
                isLiked = comment.commentUserList.find { it.user == username }?.isLiked ?: false
                isDisliked =
                    comment.commentUserList.find { it.user == username }?.isDisliked ?: false
            }

            LaunchedEffect(key1 = isLiked, key2 = isDisliked) {
                comment.like = like
                comment.dislike = dislike
                comment.commentUserList.find { it.user == username }?.isLiked = isLiked == true
                comment.commentUserList.find { it.user == username }?.isDisliked =
                    isDisliked == true
            }

            //좋아요, 싫어요 버튼 클릭시 실행될 함수
            val clickLike = {
                if (!isLiked && !isDisliked) {
                    isLiked = true
                    like += 1

                } else if (isLiked) {
                    isLiked = false
                    like -= 1
                } else {
                    isDisliked = false
                    dislike -= 1
                    isLiked = true
                    like += 1
                }
            }

            val clickDislike = {
                if (!isLiked && !isDisliked) {
                    isDisliked = true
                    dislike += 1
                } else if (isDisliked) {
                    isDisliked = false
                    dislike -= 1
                } else {
                    isLiked = false
                    like -= 1
                    isDisliked = true
                    dislike += 1
                }
            }

            //댓글
            ShowComment(
                comment = comment,
                like = like,
                dislike = dislike,
                isLiked = isLiked,
                isDisliked = isDisliked,
                clickLike = clickLike,
                clickDislike = clickDislike
            )

        }
    }
}

@Composable
fun ShowComment(
    comment: Comment,
    like: Int = comment.like,
    dislike: Int = comment.dislike,
    isLiked: Boolean,
    isDisliked: Boolean,
    clickLike: () -> Unit,
    clickDislike: () -> Unit
) {

    Column {
        // 댓글 내용
        Row(
            verticalAlignment = Alignment.Top
        ) {
            // 프로필 이미지
            Image(
                painter = comment.img,
                contentDescription = "",
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(30.dp))
            )


            // 댓글 내용
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = comment.username, fontWeight = FontWeight.Bold)
                Text(text = comment.commentText)
                Text(text = comment.date.toString(), color = Color.Gray, fontSize = 15.sp)
            }

        }


        //댓글 -> 좋아요, 싫어요
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            // 좋아요
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                imageVector = if (isLiked) Icons.Filled.ThumbUp else Icons.Filled.ThumbUpOffAlt,
                contentDescription = "Likes",
                modifier = Modifier
                    .size(20.dp)
                    .clickable {
                        clickLike()
                    }
            )

            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "$like likes")


            // 싫어요
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                imageVector = if (isDisliked) Icons.Filled.ThumbDown else Icons.Default.ThumbDownOffAlt,
                contentDescription = "Dislikes",
                modifier = Modifier
                    .size(20.dp)
                    .clickable {
                        clickDislike()
                    })

            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$dislike dislikes"
            )

        }
    }
}

@Composable
fun ShowCommentInput(modifier: Modifier, onClickSend: (String) -> Unit) {

    var userComment by rememberSaveable { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Row(
        modifier = modifier.padding(10.dp),
        verticalAlignment = Alignment.CenterVertically

    ) {

        //댓글 입력창
        TextField(
            value = userComment,
            onValueChange = { userComment = it },
            label = { Text("Comment") },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Text
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (userComment != "") {
                        onClickSend(userComment)
                        userComment = ""
                    } else {
//                        keyboardController?.hide()
                        focusManager.clearFocus()

                    }
                }
            ),
            modifier = Modifier
                .weight(1f)
                .padding(end = 10.dp)
        )

        //전송 버튼
        Button(
            onClick = {
                onClickSend(userComment)
                userComment = ""
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

    }
}