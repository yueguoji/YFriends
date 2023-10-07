package generator.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import generator.domain.UserTeam;
import generator.service.UserTeamService;
import generator.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author YUE
* @description 针对表【user_team(用户队伍关系表)】的数据库操作Service实现
* @createDate 2023-08-12 15:42:06
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




