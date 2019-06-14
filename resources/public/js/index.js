$(function(){
    console.log('loaded!')
})

function submitForm(){
    // console.log($('form'))
    // console.log($('form').serialize())
    
    const selectedFile = document.getElementById('file-input').files[0]
    console.log(selectedFile)
    $.ajax({
        url: '/api/upload',
        data: {"file" : selectedFile},
        type: 'POST',
        success: function(response) {
            // return false;
        }})
    
    return false;
}
