package study.datajpa.dto;


import lombok.Data;

@Data //단순 Dto라서 Data annotation 을 사용했음. (Entity에는 스택오버플로가 발생할 수 있으므로 사용하지 않기)
public class MemberDto {

    private Long id;
    private String username;
    private String teamName;


    public MemberDto(Long id, String username, String teamName) {
        this.id = id;
        this.username = username;
        this.teamName = teamName;
    }


}
